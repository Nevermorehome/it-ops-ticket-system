<template>
  <div class="app-container">
    <el-page-header @back="$router.back()" class="page-head">
      <template #content>
        <div class="head-content">
          <span class="no mono">{{ ticket.ticketNo }}</span>
          <el-tag :type="statusType(ticket.status)" size="small">{{ statusLabel(ticket.status) }}</el-tag>
          <el-tag :type="priorityType(ticket.priority)" size="small" effect="plain">
            {{ priorityLabel(ticket.priority) }}
          </el-tag>
          <span class="title">{{ ticket.title }}</span>
        </div>
      </template>
      <template #extra>
        <el-button
          v-if="can('EDIT')"
          v-hasPermi="['ticket:edit']"
          size="small"
          @click="$router.push(`/ticket/edit?id=${ticket.ticketId}`)"
        >
          编辑
        </el-button>
        <el-button v-if="can('ASSIGN')" v-hasPermi="['ticket:assign']" type="primary" size="small" @click="openAction('ASSIGN')">
          指派
        </el-button>
        <el-button v-if="can('TRANSFER')" v-hasPermi="['ticket:transfer']" size="small" @click="openAction('TRANSFER')">
          转派
        </el-button>
        <el-button v-if="can('COLLABORATE')" v-hasPermi="['ticket:collaborate']" size="small" @click="openAction('COLLABORATE')">
          协同
        </el-button>
        <el-button v-if="can('ACCEPT')" v-hasPermi="['ticket:accept']" type="success" size="small" @click="openAction('ACCEPT')">
          受理
        </el-button>
        <el-button v-if="can('HANDLE')" v-hasPermi="['ticket:handle']" type="primary" size="small" @click="openAction('HANDLE')">
          提交处理
        </el-button>
        <el-button v-if="can('SUSPEND')" v-hasPermi="['ticket:suspend']" type="warning" size="small" @click="openAction('SUSPEND')">
          挂起
        </el-button>
        <el-button v-if="can('RESUME')" v-hasPermi="['ticket:suspend']" type="success" size="small" @click="openAction('RESUME')">
          恢复
        </el-button>
        <el-button v-if="can('RESOLVE')" v-hasPermi="['ticket:resolve']" type="success" size="small" @click="openAction('RESOLVE')">
          解决
        </el-button>
        <el-button v-if="can('CLOSE')" v-hasPermi="['ticket:close']" size="small" @click="openAction('CLOSE')">
          关闭
        </el-button>
        <el-button v-if="can('REOPEN')" v-hasPermi="['ticket:reopen']" type="warning" size="small" @click="openAction('REOPEN')">
          重开
        </el-button>
        <el-button v-if="can('CANCEL')" v-hasPermi="['ticket:cancel']" danger size="small" @click="openAction('CANCEL')">
          取消
        </el-button>
        <el-button v-if="can('MERGE')" v-hasPermi="['ticket:merge']" size="small" @click="openAction('MERGE')">
          合并
        </el-button>
        <el-button
          v-if="['PROCESSING', 'SUSPENDED'].includes(ticket.status)"
          v-hasPermi="['ticket:field']"
          type="primary"
          plain
          size="small"
          @click="openFieldDialog"
        >
          现场记录
        </el-button>
      </template>
    </el-page-header>

    <el-row :gutter="14" style="margin-top: 14px">
      <el-col :span="16">
        <el-card shadow="never" class="block">
          <template #header>工单信息</template>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="工单分类">{{ ticket.categoryName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="报修人">{{ ticket.reporterName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ ticket.phone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="报修部门">{{ ticket.deptName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="来源">{{ sourceLabel(ticket.source) }}</el-descriptions-item>
            <el-descriptions-item label="处理人">{{ ticket.handlerName || '未指派' }}</el-descriptions-item>
            <el-descriptions-item label="协同人">
              <el-tag
                v-for="c in ticket.collaborators"
                :key="c.id"
                size="small"
                style="margin-right: 4px"
              >
                {{ c.userName }}
              </el-tag>
              <span v-if="!ticket.collaborators?.length">—</span>
            </el-descriptions-item>
            <el-descriptions-item label="关联资产">{{ ticket.assetName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ ticket.createTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="现场地点" :span="3">
              {{ [ticket.locationText, formatLngLat(ticket.longitude, ticket.latitude)].filter(Boolean).join(' ') || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="问题描述" :span="3">
              <div class="pre-wrap">{{ ticket.description || '—' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="处理情况" :span="3">
              <div class="pre-wrap">{{ ticket.handleSummary || '—' }}</div>
            </el-descriptions-item>
            <el-descriptions-item v-if="ticket.solution" label="解决方案" :span="3">
              <div class="pre-wrap">{{ ticket.solution }}</div>
            </el-descriptions-item>
            <el-descriptions-item v-if="ticket.suspendReason" label="挂起原因" :span="3">
              {{ ticket.suspendReason }}
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="ticket.attachments?.length" class="attach-block">
            <div class="block-title">建单附件</div>
            <el-image
              v-for="a in ticket.attachments"
              :key="a.attachmentId"
              :src="a.fileUrl"
              :preview-src-list="ticket.attachments.map((x: any) => x.fileUrl)"
              :initial-index="0"
              fit="cover"
              class="attach-img"
            />
          </div>
        </el-card>

        <el-card shadow="never" class="block">
          <template #header>
            <div class="card-head">
              <span>现场记录</span>
            </div>
          </template>
          <el-empty v-if="!ticket.fieldRecords?.length" description="暂无现场记录" :image-size="50" />
          <div v-for="r in ticket.fieldRecords" :key="r.recordId" class="field-record">
            <div class="record-head">
              <el-icon><LocationInformation /></el-icon>
              <span class="record-user">{{ r.createBy }}</span>
              <span class="record-time">{{ r.recordTime }}</span>
            </div>
            <div class="record-addr">
              {{ r.address || '未记录地址' }}
              <span class="record-lng" v-if="r.longitude">({{ r.longitude }}, {{ r.latitude }})</span>
            </div>
            <div class="pre-wrap">{{ r.content }}</div>
            <div v-if="r.images?.length" class="record-images">
              <el-image
                v-for="img in r.images"
                :key="img.attachmentId"
                :src="img.fileUrl"
                :preview-src-list="r.images.map((x: any) => x.fileUrl)"
                fit="cover"
                class="attach-img"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="block">
          <template #header>处理时间线</template>
          <el-timeline>
            <el-timeline-item
              v-for="line in ticket.timelines"
              :key="line.timelineId"
              :timestamp="line.createTime"
              placement="top"
              :type="line.action === 'CREATE' ? 'primary' : undefined"
            >
              <div class="line-action">
                <strong>{{ line.actionName }}</strong>
                <span class="line-user">{{ line.operateName }}</span>
              </div>
              <div class="line-content">{{ line.content }}</div>
              <div v-if="line.attachments?.length" class="line-images">
                <el-image
                  v-for="img in line.attachments"
                  :key="img.attachmentId"
                  :src="img.fileUrl"
                  :preview-src-list="line.attachments.map((x: any) => x.fileUrl)"
                  fit="cover"
                  class="line-img"
                />
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 动作对话框 -->
    <el-dialog v-model="actionVisible" :title="actionTitle" width="520px" @closed="resetActionForm">
      <el-form label-width="92px">
        <el-form-item v-if="['ASSIGN', 'TRANSFER'].includes(actionType)" label="目标人员" required>
          <el-select
            v-model="actionForm.targetUserId"
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userLoading"
            placeholder="搜索姓名/账号"
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
        <el-form-item v-if="actionType === 'TRANSFER'" label="转派原因">
          <el-input v-model="actionForm.content" type="textarea" :rows="3" placeholder="请说明转派原因" />
        </el-form-item>
        <el-form-item v-if="actionType === 'COLLABORATE'" label="协同人员" required>
          <el-select
            v-model="actionForm.collaboratorIds"
            multiple
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userLoading"
            placeholder="可选择多人"
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
        <el-form-item v-if="actionType === 'HANDLE'" label="处理情况" required>
          <el-input v-model="actionForm.content" type="textarea" :rows="4" placeholder="本次处理/进展说明" />
        </el-form-item>
        <el-form-item v-if="actionType === 'HANDLE'" label="处理图片">
          <ImageUpload v-model="actionForm.attachments" :limit="9" />
        </el-form-item>
        <el-form-item v-if="actionType === 'SUSPEND'" label="挂起原因" required>
          <el-select v-model="actionForm.suspendReason" placeholder="选择原因" style="width: 100%; margin-bottom: 8px">
            <el-option v-for="d in suspendDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
          <el-input v-model="actionForm.content" type="textarea" :rows="2" placeholder="补充说明" />
        </el-form-item>
        <el-form-item v-if="actionType === 'RESOLVE'" label="解决方案" required>
          <el-input v-model="actionForm.solution" type="textarea" :rows="4" placeholder="请填写最终解决方案" />
        </el-form-item>
        <el-form-item v-if="actionType === 'CLOSE'" label="满意度评价">
          <el-rate v-model="actionForm.satisfaction" :max="5" show-text :texts="['很不满意', '不满意', '一般', '满意', '很满意']" />
        </el-form-item>
        <el-form-item v-if="actionType === 'CLOSE' && actionForm.satisfaction" label="评价说明">
          <el-input v-model="actionForm.satisfactionComment" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item v-if="actionType === 'REOPEN'" label="重开原因">
          <el-input v-model="actionForm.content" type="textarea" :rows="3" placeholder="请说明重开原因" />
        </el-form-item>
        <el-form-item v-if="actionType === 'CANCEL'" label="取消原因">
          <el-input v-model="actionForm.content" type="textarea" :rows="3" placeholder="请说明取消原因" />
        </el-form-item>
        <el-form-item v-if="actionType === 'MERGE'" label="目标工单" required>
          <el-select
            v-model="actionForm.targetTicketId"
            filterable
            remote
            :remote-method="searchMergeTickets"
            :loading="mergeLoading"
            placeholder="输入编号/标题搜索"
            style="width: 100%"
          >
            <el-option
              v-for="t in mergeOptions"
              :key="t.ticketId"
              :label="`${t.ticketNo} ${t.title}`"
              :value="Number(t.ticketId)"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="['ACCEPT', 'RESUME'].includes(actionType)">
          <el-alert :title="`确定执行「${actionTitle}」操作吗？`" type="info" :closable="false" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAction">确定</el-button>
      </template>
    </el-dialog>

    <!-- 现场记录对话框 -->
    <el-dialog v-model="fieldVisible" title="新增现场记录" width="560px">
      <el-form :model="fieldForm" label-width="80px">
        <el-form-item label="现场地址" required>
          <el-input v-model="fieldForm.record.address" placeholder="如：行政楼 3 楼 305" />
        </el-form-item>
        <el-form-item label="经纬度">
          <el-row :gutter="8">
            <el-col :span="12">
              <el-input v-model.number="fieldForm.record.longitude" placeholder="经度" />
            </el-col>
            <el-col :span="12">
              <el-input v-model.number="fieldForm.record.latitude" placeholder="纬度" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="处理记录" required>
          <el-input v-model="fieldForm.record.content" type="textarea" :rows="4" placeholder="现场处理情况" />
        </el-form-item>
        <el-form-item label="现场照片">
          <ImageUpload v-model="fieldForm.images" :limit="9" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fieldVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitField">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import { addFieldRecord, doAction, getTicket, pageTicket, userOptions as fetchUserOptions } from '@/api/ticket'
import { fallbackDict, useDictData, type DictItem } from '@/composables/useDict'

const route = useRoute()
const ticketId = route.params.id as string

const ticket = ref<any>({})
const loading = ref(false)

let statusDict: DictItem[] = fallbackDict('ticket_status')
let priorityDict: DictItem[] = fallbackDict('ticket_priority')
let sourceDict: DictItem[] = fallbackDict('ticket_source')
let suspendDict: DictItem[] = fallbackDict('suspend_reason')

const ALLOWED: Record<string, string[]> = {
  EDIT: ['PENDING', 'ASSIGNED'],
  ASSIGN: ['PENDING'],
  TRANSFER: ['ASSIGNED', 'PROCESSING', 'SUSPENDED'],
  COLLABORATE: ['PENDING', 'ASSIGNED', 'PROCESSING', 'SUSPENDED', 'RESOLVED'],
  ACCEPT: ['ASSIGNED'],
  HANDLE: ['PROCESSING'],
  SUSPEND: ['PROCESSING'],
  RESUME: ['SUSPENDED'],
  RESOLVE: ['PROCESSING'],
  CLOSE: ['RESOLVED'],
  REOPEN: ['CLOSED'],
  CANCEL: ['PENDING', 'ASSIGNED'],
  MERGE: ['PENDING', 'ASSIGNED', 'PROCESSING', 'SUSPENDED', 'RESOLVED']
}
function can(action: string) {
  return (ALLOWED[action] || []).includes(ticket.value.status)
}

function statusLabel(s: string) {
  return statusDict.find((d) => d.dictValue === s)?.dictLabel || s
}
function statusType(s: string): any {
  return (statusDict.find((d) => d.dictValue === s)?.listClass || 'info') as any
}
function priorityLabel(p: number) {
  return priorityDict.find((d) => Number(d.dictValue) === p)?.dictLabel || '—'
}
function priorityType(p: number): any {
  return (priorityDict.find((d) => Number(d.dictValue) === p)?.listClass || 'info') as any
}
function sourceLabel(s: string) {
  return sourceDict.find((d) => d.dictValue === s)?.dictLabel || s || '—'
}
function formatLngDate(lng: any, lat: any) {
  return lng && lat ? `(${lng}, ${lat})` : ''
}
const formatLngLat = formatLngDate

async function load() {
  loading.value = true
  try {
    const res = await getTicket(ticketId)
    ticket.value = res.data || {}
  } finally {
    loading.value = false
  }
}

// ---------------- 动作 ----------------
const actionVisible = ref(false)
const actionType = ref('')
const submitting = ref(false)
const actionTitle = ref('')
const actionForm = reactive<any>({
  targetUserId: undefined,
  collaboratorIds: [],
  content: '',
  solution: '',
  suspendReason: '',
  satisfaction: 0,
  satisfactionComment: '',
  targetTicketId: undefined,
  attachments: []
})

const userOptions = ref<any[]>([])
const userLoading = ref(false)
const mergeOptions = ref<any[]>([])
const mergeLoading = ref(false)

async function searchUsers(keyword: string) {
  userLoading.value = true
  try {
    const res = await fetchUserOptions(keyword)
    userOptions.value = res.data || []
  } finally {
    userLoading.value = false
  }
}
async function searchMergeTickets(keyword: string) {
  if (!keyword) return
  mergeLoading.value = true
  try {
    const res = await pageTicket({ pageNum: 1, pageSize: 20, keyword })
    mergeOptions.value = (res.data.records || []).filter(
      (t: any) =>
        String(t.ticketId) !== String(ticketId) && !['CLOSED', 'CANCELLED', 'MERGED'].includes(t.status)
    )
  } finally {
    mergeLoading.value = false
  }
}

function openAction(type: string) {
  actionType.value = type
  const titles: Record<string, string> = {
    ASSIGN: '工单指派',
    TRANSFER: '工单转派',
    COLLABORATE: '设置协同人',
    ACCEPT: '受理工单',
    HANDLE: '提交处理记录',
    SUSPEND: '挂起工单',
    RESUME: '恢复工单',
    RESOLVE: '解决工单',
    CLOSE: '关闭工单',
    REOPEN: '重开工单',
    CANCEL: '取消工单',
    MERGE: '合并工单'
  }
  actionTitle.value = titles[type]
  if (['ASSIGN', 'TRANSFER', 'COLLABORATE'].includes(type)) {
    searchUsers('')
  }
  if (type === 'MERGE') {
    searchMergeTickets('')
  }
  if (type === 'COLLABORATE' && ticket.value.collaborators) {
    actionForm.collaboratorIds = ticket.value.collaborators.map((c: any) => Number(c.userId))
  }
  actionVisible.value = true
}

function resetActionForm() {
  Object.assign(actionForm, {
    targetUserId: undefined,
    collaboratorIds: [],
    content: '',
    solution: '',
    suspendReason: '',
    satisfaction: 0,
    satisfactionComment: '',
    targetTicketId: undefined,
    attachments: []
  })
  actionType.value = ''
}

async function submitAction() {
  const t = actionType.value
  if (['ASSIGN', 'TRANSFER'].includes(t) && !actionForm.targetUserId) {
    ElMessage.warning('请选择目标人员')
    return
  }
  if (t === 'COLLABORATE' && !actionForm.collaboratorIds.length) {
    ElMessage.warning('请选择协同人')
    return
  }
  if (t === 'HANDLE' && !actionForm.content?.trim()) {
    ElMessage.warning('请填写处理情况')
    return
  }
  if (t === 'SUSPEND' && !actionForm.suspendReason && !actionForm.content?.trim()) {
    ElMessage.warning('请填写挂起原因')
    return
  }
  if (t === 'RESOLVE' && !actionForm.solution?.trim()) {
    ElMessage.warning('请填写解决方案')
    return
  }
  if (t === 'MERGE' && !actionForm.targetTicketId) {
    ElMessage.warning('请选择目标工单')
    return
  }
  submitting.value = true
  try {
    await doAction(t.toLowerCase(), {
      ticketId: Number(ticketId),
      ...JSON.parse(JSON.stringify(actionForm)),
      satisfaction: actionForm.satisfaction > 0 ? actionForm.satisfaction : undefined
    })
    ElMessage.success('操作成功')
    actionVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

// ---------------- 现场记录 ----------------
const fieldVisible = ref(false)
const fieldForm = reactive<{ record: any; images: any[] }>({
  record: { address: '', longitude: undefined, latitude: undefined, content: '' },
  images: []
})
function openFieldDialog() {
  fieldForm.record = { address: ticket.value.locationText || '', longitude: ticket.value.longitude, latitude: ticket.value.latitude, content: '' }
  fieldForm.images = []
  fieldVisible.value = true
}
async function submitField() {
  if (!fieldForm.record.address?.trim() || !fieldForm.record.content?.trim()) {
    ElMessage.warning('请填写现场地址与处理记录')
    return
  }
  submitting.value = true
  try {
    await addFieldRecord(Number(ticketId), fieldForm.record, fieldForm.images)
    ElMessage.success('现场记录已提交')
    fieldVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  statusDict = await useDictData('ticket_status')
  priorityDict = await useDictData('ticket_priority')
  sourceDict = await useDictData('ticket_source')
  suspendDict = await useDictData('suspend_reason')
  load()
})
</script>

<style scoped>
.page-head {
  background: #fff;
  padding: 14px 18px;
  border-radius: 6px;
}
.head-content {
  display: flex;
  align-items: center;
  gap: 10px;
}
.no {
  color: #909399;
  font-size: 13px;
}
.title {
  font-weight: 600;
  font-size: 15px;
}
.block {
  margin-bottom: 14px;
}
.pre-wrap {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.7;
}
.attach-block {
  margin-top: 12px;
}
.block-title {
  font-weight: 600;
  margin-bottom: 8px;
}
.attach-img {
  width: 96px;
  height: 96px;
  border-radius: 4px;
  margin: 0 8px 8px 0;
  cursor: pointer;
}
.field-record {
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
}
.record-head {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #409eff;
}
.record-user {
  font-weight: 600;
  color: #303133;
}
.record-time {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}
.record-addr {
  margin: 6px 0;
  font-size: 13px;
}
.record-lng {
  color: #c0c4cc;
  font-size: 12px;
}
.record-images {
  margin-top: 8px;
}
.line-action {
  display: flex;
  align-items: center;
  gap: 8px;
}
.line-user {
  color: #909399;
  font-size: 12px;
}
.line-content {
  margin-top: 4px;
  font-size: 13px;
  color: #606266;
}
.line-img {
  width: 60px;
  height: 60px;
  margin: 6px 6px 0 0;
  border-radius: 4px;
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
