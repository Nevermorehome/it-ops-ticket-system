<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="head">
          <el-button :icon="ArrowLeft" link @click="$router.back()">返回</el-button>
          <span>{{ isEdit ? '编辑工单' : '新建工单' }}</span>
          <span v-if="isEdit" class="no mono">{{ form.ticketNo }}</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px" style="max-width: 860px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工单标题" prop="title">
              <el-input v-model="form.title" placeholder="简要描述故障/需求" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单分类" prop="categoryId">
              <el-tree-select
                v-model="form.categoryId"
                :data="categoryTree"
                :props="{ label: 'categoryName', value: 'categoryId', children: 'children' }"
                check-strictly
                placeholder="请选择分类"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="报修人" v-if="!isEdit">
              <el-select
                v-model="form.reporterId"
                filterable
                remote
                :remote-method="searchUsers"
                :loading="userLoading"
                placeholder="默认为本人"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="u in userOptions"
                  :key="u.userId"
                  :label="`${u.realName}(${u.username})`"
                  :value="Number(u.userId)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="联系电话" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="报修部门" v-if="!isEdit">
              <el-tree-select
                v-model="form.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
                check-strictly
                placeholder="默认本人部门"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源" prop="source">
              <el-select v-model="form.source" placeholder="请选择" style="width: 100%">
                <el-option v-for="d in sourceDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="d in priorityDict"
                  :key="d.dictValue"
                  :label="d.dictLabel"
                  :value="Number(d.dictValue)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="关联资产">
              <el-select
                v-model="form.assetId"
                filterable
                remote
                :remote-method="searchAssets"
                :loading="assetLoading"
                placeholder="资产编号/名称"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="a in assetOptions"
                  :key="a.assetId"
                  :label="`${a.assetNo || ''} ${a.assetName}`"
                  :value="Number(a.assetId)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="现场地点">
              <el-select
                v-model="form.locationId"
                filterable
                placeholder="选择常用地点"
                clearable
                style="width: 100%"
                @change="onLocationChange"
              >
                <el-option
                  v-for="l in locations"
                  :key="l.locationId"
                  :label="`${l.locationName}${l.room ? '(' + l.room + ')' : ''}`"
                  :value="Number(l.locationId)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地点描述">
              <el-input v-model="form.locationText" placeholder="楼栋/楼层/房间等" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input v-model.number="form.longitude" placeholder="可选, 如 116.397428" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input v-model.number="form.latitude" placeholder="可选, 如 39.90923" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="问题描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="5"
                placeholder="请详细描述故障现象、影响范围等"
                maxlength="2000"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="附件图片">
              <ImageUpload v-model="form.attachments" :limit="9" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="onSubmit">提 交</el-button>
          <el-button @click="$router.back()">取 消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import { createTicket, getTicket, updateTicket, userOptions as fetchUserOptions } from '@/api/ticket'
import { assetApi, categoryApi, locationApi } from '@/api/base'
import { deptApi } from '@/api/system'
import { useDictData, fallbackDict, type DictItem } from '@/composables/useDict'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const saving = ref(false)
const isEdit = !!route.query.id
const id = route.query.id

const form = reactive<any>({
  ticketId: undefined,
  title: '',
  categoryId: undefined,
  reporterId: undefined,
  deptId: undefined,
  phone: '',
  source: 'phone',
  priority: 1,
  locationId: undefined,
  locationText: '',
  longitude: undefined,
  latitude: undefined,
  assetId: undefined,
  description: '',
  attachments: []
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  source: [{ required: true, message: '请选择来源', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  description: [{ required: true, message: '请描述问题', trigger: 'blur' }]
}

const categoryTree = ref<any[]>([])
const deptTree = ref<any[]>([])
const locations = ref<any[]>([])
const userOptions = ref<any[]>([])
const assetOptions = ref<any[]>([])
const userLoading = ref(false)
const assetLoading = ref(false)
let sourceDict: DictItem[] = fallbackDict('ticket_source')
let priorityDict: DictItem[] = fallbackDict('ticket_priority')

async function searchUsers(keyword: string) {
  userLoading.value = true
  try {
    const res = await fetchUserOptions(keyword)
    userOptions.value = res.data || []
  } finally {
    userLoading.value = false
  }
}
async function searchAssets(keyword: string) {
  assetLoading.value = true
  try {
    const res = await assetApi.page({ pageNum: 1, pageSize: 20, keyword })
    assetOptions.value = res.data.records || []
  } finally {
    assetLoading.value = false
  }
}
function onLocationChange(locationId: number) {
  const loc = locations.value.find((l) => Number(l.locationId) === Number(locationId))
  if (loc) {
    form.locationText = [loc.building, loc.floor, loc.room].filter(Boolean).join(' ') || loc.address || loc.locationName
    if (loc.longitude) form.longitude = Number(loc.longitude)
    if (loc.latitude) form.latitude = Number(loc.latitude)
  }
}

async function onSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (isEdit) {
      await updateTicket({ ...form })
      ElMessage.success('工单已更新')
    } else {
      const { attachments, ...ticket } = form
      await createTicket(ticket, attachments)
      ElMessage.success('工单创建成功')
    }
    router.push('/ticket/list')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  sourceDict = await useDictData('ticket_source')
  priorityDict = await useDictData('ticket_priority')
  const [cat, dept, loc] = await Promise.all([
    categoryApi.tree(),
    deptApi.tree(),
    locationApi.list()
  ])
  categoryTree.value = cat.data || []
  deptTree.value = dept.data || []
  locations.value = loc.data || []

  searchUsers('')
  searchAssets('')

  if (isEdit) {
    const res = await getTicket(id as string)
    const t = res.data
    Object.assign(form, {
      ticketId: t.ticketId,
      title: t.title,
      categoryId: t.categoryId ? Number(t.categoryId) : undefined,
      phone: t.phone || '',
      source: t.source || 'phone',
      priority: t.priority ?? 1,
      locationId: t.locationId ? Number(t.locationId) : undefined,
      locationText: t.locationText || '',
      longitude: t.longitude ? Number(t.longitude) : undefined,
      latitude: t.latitude ? Number(t.latitude) : undefined,
      assetId: t.assetId ? Number(t.assetId) : undefined,
      description: t.description || '',
      attachments: t.attachments || []
    })
  }
})
</script>

<style scoped>
.head {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
}
.no {
  color: #909399;
  font-size: 13px;
  font-weight: 400;
}
</style>
