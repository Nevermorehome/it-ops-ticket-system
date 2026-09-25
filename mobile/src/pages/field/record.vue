<template>
  <view class="page">
    <!-- 定位信息 -->
    <view class="card">
      <view class="loc-row">
        <view class="loc-info">
          <text class="loc-label">现场位置</text>
          <text v-if="record.longitude" class="loc-lng">{{ record.longitude.toFixed(6) }}, {{ record.latitude.toFixed(6) }}</text>
          <text v-else class="muted">未获取定位</text>
        </view>
        <view class="loc-btns">
          <button class="mini-btn" @tap="getLocation">GPS 定位</button>
          <!-- #ifdef APP-PLUS || MP-WEIXIN -->
          <button class="mini-btn" @tap="chooseLocationPoint">地图选点</button>
          <!-- #endif -->
        </view>
      </view>
      <input v-model="record.address" class="addr-input" placeholder="现场地址（必填，如：行政楼 3 楼 305）" />
      <view class="loc-time">
        <text class="muted">记录时间：{{ nowText }}（服务器将再次落时间）</text>
      </view>
    </view>

    <!-- 处理记录 -->
    <view class="card">
      <textarea v-model="record.content" class="content-ta" placeholder="现场处理情况（必填）" maxlength="1000" />
    </view>

    <!-- 现场照片 -->
    <view class="card">
      <view class="flex-between">
        <text>现场照片（自动附加时间/地址/经纬度/操作人水印，最多 9 张）</text>
      </view>
      <view class="img-grid">
        <view v-for="(img, i) in images" :key="i" class="img-item">
          <image :src="img.watermarkPath" mode="aspectFill" class="img" @tap="preview(i)" />
          <view v-if="img.busy" class="mask">
            <text class="mask-text">{{ img.statusText }}</text>
          </view>
          <view class="del" @tap="removeImage(i)">×</view>
        </view>
        <view v-if="images.length < 9" class="img-add" @tap="chooseImage">
          <text class="plus">＋</text>
          <text class="add-text">拍照/相册</text>
        </view>
      </view>
    </view>

    <view class="footer">
      <button class="submit" :loading="submitting" @tap="submit">提交现场记录</button>
    </view>

    <!-- 离屏水印画布（尺寸随图片动态调整，避免竖图被裁剪） -->
    <canvas
      canvas-id="watermarkCanvas"
      class="watermark-canvas"
      :style="{ width: canvasSize.w + 'px', height: canvasSize.h + 'px' }"
    ></canvas>
  </view>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { ticketApi } from '@/api'
import { BASE_URL } from '@/config'
import { useUserStore } from '@/store/user'

interface FieldImage {
  originPath: string
  watermarkPath: string
  busy: boolean
  statusText: string
  result?: any
}

const userStore = useUserStore()
const ticketId = ref('')
const submitting = ref(false)
const images = ref<FieldImage[]>([])

const record = reactive<any>({
  address: '',
  longitude: undefined,
  latitude: undefined,
  content: ''
})

const canvasSize = reactive({ w: 1080, h: 1440 })

const now = ref(new Date())
let timer: any
const pad = (n: number) => String(n).padStart(2, '0')
const nowText = computed(() => {
  const d = now.value
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
})

onLoad((options) => {
  timer = setInterval(() => (now.value = new Date()), 1000)
  ticketId.value = options?.id || ''
})

// ---------------- 定位 ----------------
function getLocation() {
  uni.showLoading({ title: '定位中', mask: true })
  uni.getLocation({
    type: 'gcj02',
    isHighAccuracy: true,
    success: (res) => {
      record.longitude = res.longitude
      record.latitude = res.latitude
      if (!record.address) record.address = res.address || ''
      uni.showToast({ title: '定位成功', icon: 'success' })
    },
    fail: () => {
      uni.showToast({ title: '定位失败，请检查定位权限', icon: 'none' })
    },
    complete: () => uni.hideLoading()
  })
}

function chooseLocationPoint() {
  uni.chooseLocation({
    success: (res) => {
      record.longitude = res.longitude
      record.latitude = res.latitude
      record.address = res.address || res.name || ''
    },
    fail: () => {
      uni.showToast({ title: '地图选点失败(需配置地图 Key)', icon: 'none' })
    }
  })
}

// ---------------- 选图/压缩/水印 ----------------
function chooseImage() {
  const remain = 9 - images.value.length
  uni.chooseImage({
    count: remain,
    sizeType: ['original', 'compressed'],
    sourceType: ['camera', 'album'],
    success: async (res) => {
      for (const path of res.tempFilePaths) {
        const item = reactive<FieldImage>({
          originPath: path,
          watermarkPath: path,
          busy: true,
          statusText: '压缩中'
        })
        images.value.push(item)
        try {
          const compressed = await compress(path)
          item.originPath = compressed
          item.statusText = '加水印'
          const watermarked = await drawWatermark(compressed)
          item.watermarkPath = watermarked
          item.busy = false
        } catch (e) {
          item.busy = false
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
      compressedWidth: 1080,
      success: (r) => resolve(r.tempFilePath),
      fail: () => resolve(src)
    })
  })
}

/**
 * canvas 水印: 时间(Asia/Shanghai, 设备本地时间) + 地址 + 经纬度 + 操作人
 */
function drawWatermark(src: string): Promise<string> {
  return new Promise((resolve, reject) => {
    uni.getImageInfo({
      src,
      success: async (info) => {
        const maxW = 1080
        const w = Math.min(info.width, maxW)
        const scale = w / info.width
        const h = Math.round(info.height * scale)
        const bandH = Math.max(150, Math.round(h * 0.16))
        const fontSize = Math.max(20, Math.round(w / 42))
        const lineH = fontSize * 1.5

        // 先把画布调整为图片真实尺寸，等待 DOM 更新后再绘制
        canvasSize.w = w
        canvasSize.h = h
        await nextTick()

        const ctx = uni.createCanvasContext('watermarkCanvas')
        ctx.clearRect(0, 0, w, h)
        ctx.drawImage(src, 0, 0, w, h)
        // 底部半透明黑底
        ctx.setFillStyle('rgba(0,0,0,0.55)')
        ctx.fillRect(0, h - bandH, w, bandH)
        ctx.setFillStyle('#ffffff')
        ctx.setFontSize(fontSize)
        const padX = fontSize
        let y = h - bandH + lineH
        const operator = userStore.realName || userStore.user.username || ''
        const lines = [
          `时间：${nowText.value}`,
          `地址：${record.address || '未填写地址'}`,
          `经纬度：${record.longitude ? record.longitude.toFixed(6) : '--'} , ${record.latitude ? record.latitude.toFixed(6) : '--'}`,
          `操作人：${operator}`
        ]
        lines.forEach((line) => {
          ctx.fillText(line, padX, y)
          y += lineH
        })
        ctx.draw(false, () => {
          setTimeout(() => {
            uni.canvasToTempFilePath({
              canvasId: 'watermarkCanvas',
              x: 0,
              y: 0,
              width: w,
              height: h,
              destWidth: w,
              destHeight: h,
              fileType: 'jpg',
              quality: 0.85,
              success: (r) => resolve(r.tempFilePath),
              fail: reject
            })
          }, 300)
        })
      },
      fail: reject
    })
  })
}

function preview(index: number) {
  uni.previewImage({
    urls: images.value.map((i) => i.watermarkPath),
    current: images.value[index].watermarkPath
  })
}
function removeImage(i: number) {
  images.value.splice(i, 1)
}

// ---------------- 提交(顺序上传) ----------------
async function uploadOne(path: string): Promise<any> {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('itops_token')
    uni.uploadFile({
      url: BASE_URL + '/file/upload',
      filePath: path,
      name: 'file',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        try {
          const body = JSON.parse(res.data)
          if (body.code === 200) resolve(body.data)
          else reject(new Error(body.msg))
        } catch (e) {
          reject(e)
        }
      },
      fail: reject
    })
  })
}

async function submit() {
  if (!record.address.trim()) {
    uni.showToast({ title: '请填写现场地址', icon: 'none' })
    return
  }
  if (!record.content.trim()) {
    uni.showToast({ title: '请填写处理情况', icon: 'none' })
    return
  }
  if (images.value.some((i) => i.busy)) {
    uni.showToast({ title: '图片处理中，请稍候', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    // 顺序上传水印图
    const uploaded: any[] = []
    for (const img of images.value) {
      if (img.result) {
        uploaded.push(img.result)
        continue
      }
      const result = await uploadOne(img.watermarkPath)
      img.result = result
      uploaded.push(result)
    }
    await ticketApi.addFieldRecord(
      ticketId.value,
      {
        address: record.address,
        longitude: record.longitude,
        latitude: record.latitude,
        content: record.content
      },
      uploaded
    )
    uni.showToast({ title: '现场记录已提交', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 600)
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page {
  padding-bottom: 160rpx;
}
.loc-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.loc-label {
  font-weight: 600;
  display: block;
  margin-bottom: 8rpx;
}
.loc-lng {
  font-size: 26rpx;
  color: #2b6cb0;
}
.loc-btns {
  display: flex;
  gap: 12rpx;
}
.mini-btn {
  font-size: 24rpx;
  padding: 0 22rpx;
  height: 60rpx;
  line-height: 60rpx;
  background: #2b6cb0;
  color: #fff;
  border-radius: 30rpx;
  margin: 0;
}
.mini-btn::after {
  border: none;
}
.addr-input {
  margin-top: 18rpx;
  background: #f7f8fa;
  border-radius: 10rpx;
  padding: 20rpx;
}
.loc-time {
  margin-top: 12rpx;
}
.content-ta {
  width: 100%;
  box-sizing: border-box;
  min-height: 240rpx;
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
  font-size: 26rpx;
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
.watermark-canvas {
  position: fixed;
  left: -9999px;
  top: -9999px;
}
.muted {
  color: #909399;
  font-size: 24rpx;
}
</style>
