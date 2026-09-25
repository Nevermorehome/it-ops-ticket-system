<template>
  <view class="page">
    <view class="card">
      <view class="form-item">
        <text class="label"><text class="req">*</text>标题</text>
        <input v-model="form.title" class="input" placeholder="简要描述故障/需求" maxlength="100" />
      </view>

      <view class="form-item">
        <text class="label"><text class="req">*</text>分类</text>
        <picker :range="flatCategories" range-key="categoryName" @change="onCategoryChange">
          <view class="picker">{{ form.categoryName || '请选择分类' }}</view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label"><text class="req">*</text>优先级</text>
        <view class="seg">
          <view
            v-for="p in priorityDict"
            :key="p.dictValue"
            class="seg-item"
            :class="{ active: form.priority === Number(p.dictValue) }"
            @tap="form.priority = Number(p.dictValue)"
          >
            {{ p.dictLabel }}
          </view>
        </view>
      </view>

      <view class="form-item">
        <text class="label">来源</text>
        <picker :range="sourceDict" range-key="dictLabel" @change="onSourceChange">
          <view class="picker">{{ sourceLabel || '请选择来源' }}</view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label"><text class="req">*</text>联系电话</text>
        <input v-model="form.phone" type="number" class="input" placeholder="联系电话" maxlength="20" />
      </view>

      <view class="form-item">
        <text class="label">常用地点</text>
        <picker :range="locations" range-key="locationName" @change="onLocationChange">
          <view class="picker">{{ locationLabel || '选择常用地点(可选)' }}</view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">地点描述</text>
        <input v-model="form.locationText" class="input" placeholder="楼栋/楼层/房间" maxlength="100" />
      </view>

      <view class="form-item align-top">
        <text class="label"><text class="req">*</text>问题描述</text>
        <textarea
          v-model="form.description"
          class="textarea"
          placeholder="请详细描述故障现象、影响范围"
          maxlength="2000"
        />
      </view>
    </view>

    <view class="card">
      <text class="label">现场图片（最多 9 张）</text>
      <view class="img-grid">
        <view v-for="(img, i) in images" :key="i" class="img-item">
          <image :src="img.localPath" mode="aspectFill" class="img" />
          <view v-if="img.uploading" class="mask">
            <text class="mask-text">{{ img.progress }}%</text>
          </view>
          <view class="del" @tap.stop="removeImage(i)">×</view>
        </view>
        <view v-if="images.length < 9" class="img-add" @tap="chooseImage">
          <text class="plus">＋</text>
          <text class="add-text">拍照/相册</text>
        </view>
      </view>
    </view>

    <view class="footer">
      <button class="submit" :loading="submitting" @tap="submit">提交工单</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { baseApi, ticketApi } from '@/api'
import { BASE_URL } from '@/config'
import { loadDict, type DictItem } from '@/utils/dict'

interface PickedImage {
  localPath: string
  progress: number
  uploading: boolean
  result?: any
}

const form = reactive<any>({
  title: '',
  categoryId: undefined,
  categoryName: '',
  priority: 1,
  source: 'self',
  phone: '',
  locationId: undefined,
  locationText: '',
  longitude: undefined,
  latitude: undefined,
  description: ''
})

const priorityDict = ref<DictItem[]>([])
const sourceDict = ref<DictItem[]>([])
const categories = ref<any[]>([])
const locations = ref<any[]>([])
const images = ref<PickedImage[]>([])
const submitting = ref(false)

const flatCategories = computed(() => {
  const out: any[] = []
  const walk = (nodes: any[], depth: number) => {
    nodes.forEach((n) => {
      out.push({ ...n, categoryName: '　'.repeat(depth) + n.categoryName })
      if (n.children?.length) walk(n.children, depth + 1)
    })
  }
  walk(categories.value, 0)
  return out
})
const sourceLabel = computed(
  () => sourceDict.value.find((d) => d.dictValue === form.source)?.dictLabel
)
const locationLabel = computed(() => locations.value.find((l) => Number(l.locationId) === Number(form.locationId))?.locationName)

function onCategoryChange(e: any) {
  const c = flatCategories.value[Number(e.detail.value)]
  form.categoryId = Number(c.categoryId)
  form.categoryName = c.categoryName.trim()
}
function onSourceChange(e: any) {
  form.source = sourceDict.value[Number(e.detail.value)].dictValue
}
function onLocationChange(e: any) {
  const l = locations.value[Number(e.detail.value)]
  form.locationId = Number(l.locationId)
  form.locationText = [l.building, l.floor, l.room].filter(Boolean).join(' ') || l.address || l.locationName
  if (l.longitude) form.longitude = Number(l.longitude)
  if (l.latitude) form.latitude = Number(l.latitude)
}

function chooseImage() {
  const remain = 9 - images.value.length
  uni.chooseImage({
    count: remain,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      for (const path of res.tempFilePaths) {
        const picked: PickedImage = reactive({ localPath: path, progress: 0, uploading: true })
        images.value.push(picked)
        try {
          // 先压缩再上传
          const compressed = await compress(path)
          await uploadOne(compressed, picked)
        } catch {
          picked.uploading = false
          uni.showToast({ title: '图片处理失败', icon: 'none' })
        }
      }
    }
  })
}

function compress(src: string): Promise<string> {
  return new Promise((resolve) => {
    uni.compressImage({
      src,
      quality: 70,
      success: (r) => resolve(r.tempFilePath),
      fail: () => resolve(src)
    })
  })
}

function uploadOne(path: string, picked: PickedImage) {
  return new Promise<void>((resolve, reject) => {
    const token = uni.getStorageSync('itops_token')
    const task = uni.uploadFile({
      url: BASE_URL + '/file/upload',
      filePath: path,
      name: 'file',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        try {
          const body = JSON.parse(res.data)
          if (body.code === 200) {
            picked.result = body.data
            picked.progress = 100
            picked.uploading = false
            resolve()
          } else {
            reject(new Error(body.msg))
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: reject
    })
    task.onProgressUpdate((p) => {
      picked.progress = p.progress
    })
  })
}

function removeImage(i: number) {
  images.value.splice(i, 1)
}

async function submit() {
  if (!form.title || !form.categoryId || !form.phone || !form.description) {
    uni.showToast({ title: '请完善必填项', icon: 'none' })
    return
  }
  if (images.value.some((i) => i.uploading)) {
    uni.showToast({ title: '图片上传中，请稍候', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const attachments = images.value.filter((i) => i.result).map((i) => i.result)
    const payload = { ...form }
    delete payload.categoryName
    await ticketApi.create(payload, attachments)
    uni.showToast({ title: '提交成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 600)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  priorityDict.value = await loadDict('ticket_priority')
  sourceDict.value = await loadDict('ticket_source')
  try {
    const [cat, loc] = await Promise.all([baseApi.categoryTree(), baseApi.locationList()])
    categories.value = cat || []
    locations.value = loc || []
  } catch {
    // ignore
  }
})
</script>

<style lang="scss" scoped>
.page {
  padding-bottom: 160rpx;
}
.form-item {
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #f2f3f5;
  padding: 24rpx 0;
}
.form-item.align-top {
  align-items: flex-start;
}
.form-item:last-child {
  border-bottom: none;
}
.label {
  width: 170rpx;
  color: #303133;
  font-size: 28rpx;
  flex-shrink: 0;
}
.req {
  color: #f56c6c;
}
.input {
  flex: 1;
}
.textarea {
  flex: 1;
  min-height: 180rpx;
  width: 100%;
}
.picker {
  flex: 1;
  color: #303133;
}
.seg {
  display: flex;
  gap: 12rpx;
  flex: 1;
}
.seg-item {
  padding: 8rpx 22rpx;
  border-radius: 8rpx;
  background: #f4f5f7;
  font-size: 26rpx;
  color: #606266;
}
.seg-item.active {
  background: rgba(43, 108, 176, 0.12);
  color: #2b6cb0;
  font-weight: 600;
}
.img-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 20rpx;
}
.img-item,
.img-add {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  position: relative;
}
.img {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
}
.img-add {
  border: 2rpx dashed #c0c4cc;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}
.plus {
  font-size: 60rpx;
  line-height: 1;
}
.add-text {
  font-size: 22rpx;
  margin-top: 8rpx;
}
.mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.mask-text {
  color: #fff;
  font-size: 28rpx;
}
.del {
  position: absolute;
  top: -14rpx;
  right: -14rpx;
  width: 40rpx;
  height: 40rpx;
  line-height: 36rpx;
  text-align: center;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border-radius: 50%;
  font-size: 28rpx;
}
.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 40rpx calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.05);
}
.submit {
  background: #2b6cb0;
  color: #fff;
  border-radius: 12rpx;
}
</style>
