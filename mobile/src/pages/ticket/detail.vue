<template>
  <view class="page" v-if="ticket.ticketId">
    <!-- 状态头 -->
    <view class="head" :style="{ background: statusColor(ticket.status) }">
      <view class="head-row">
        <text class="no">{{ ticket.ticketNo }}</text>
        <text class="status-text">{{ statusLabel(ticket.status) }}</text>
      </view>
      <view class="head-title">{{ ticket.title }}</view>
      <view class="head-sub">
        <text>{{ priorityLabel(ticket.priority) }}优先级</text>
        <text> · {{ ticket.categoryName || '未分类' }}</text>
        <text> · {{ sourceLabel(ticket.source) }}</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <scroll-view scroll-x class="action-bar">
      <view class="action-inner">
        <button v-if="can('ASSIGN')" class="act" @tap="openAction('ASSIGN')">指派</button>
        <button v-if="can('TRANSFER')" class="act" @tap="openAction('TRANSFER')">转派</button>
        <button v-if="can('COLLABORATE')" class="act" @tap="openAction('COLLABORATE')">协同</button>
        <button v-if="can('ACCEPT')" class="act act-success" @tap="confirmAction('ACCEPT')">受理</button>
        <button v-if="can('HANDLE')" class="act act-primary" @tap="openAction('HANDLE')">提交处理</button>
        <button v-if="can('SUSPEND')" class="act act-warning" @tap="openAction('SUSPEND')">挂起</button>
        <button v-if="can('RESUME')" class="act act-success" @tap="confirmAction('RESUME')">恢复</button>
        <button v-if="can('RESOLVE')" class="act act-success" @tap="openAction('RESOLVE')">解决</button>
        <button v-if="can('CLOSE')" class="act" @tap="openAction('CLOSE')">关闭</button>
        <button v-if="can('REOPEN')" class="act act-warning" @tap="openAction('REOPEN')">重开</button>
        <button v-if="can('CANCEL')" class="act act-danger" @tap="openAction('CANCEL')">取消</button>
        <button v-if="can('MERGE')" class="act" @tap="openAction('MERGE')">合并</button>
        <button
          v-if="['PROCESSING', 'SUSPENDED'].includes(ticket.status)"
          class="act act-primary"
          @tap="goField"
        >
          现场记录
        </button>
      </view>
    </scroll-view>

    <!-- 信息 -->
    <view class="card">
      <view class="info-row"><text class="k">报修人</text><text class="v">{{ ticket.reporterName || '—' }}</text></view>
      <view class="info-row"><text class="k">部门</text><text class="v">{{ ticket.deptName || '—' }}</text></view>
      <view class="info-row"><text class="k">电话</text><text class="v">{{ ticket.phone || '—' }}</text></view>
      <view class="info-row"><text class="k">处理人</text><text class="v">{{ ticket.handlerName || '未指派' }}</text></view>
      <view class="info-row">
        <text class="k">协同人</text>
        <text class="v">{{ (ticket.collaborators || []).map((c: any) => c.userName).join('、') || '—' }}</text>
      </view>
      <view class="info-row"><text class="k">资产</text><text class="v">{{ ticket.assetName || '—' }}</text></view>
      <view class="info-row"><text class="k">地点</text><text class="v">{{ ticket.locationText || '—' }}</text></view>
      <view class="info-row" v-if="ticket.longitude">
        <text class="k">经纬度</text>
        <text class="v">{{ ticket.longitude }}, {{ ticket.latitude }}</text>
      </view>
      <view class="info-row"><text class="k">创建时间</text><text class="v">{{ ticket.createTime }}</text></view>
      <view class="block-title">问题描述</view>
      <view class="pre">{{ ticket.description || '—' }}</view>
      <view v-if="ticket.handleSummary" class="block-title">处理情况</view>
      <view v-if="ticket.handleSummary" class="pre">{{ ticket.handleSummary }}</view>
      <view v-if="ticket.solution" class="block-title">解决方案</view>
      <view v-if="ticket.solution" class="pre success-pre">{{ ticket.solution }}</view>
      <view v-if="ticket.suspendReason" class="block-title">挂起原因</view>
      <view v-if="ticket.suspendReason" class="pre">{{ ticket.suspendReason }}</view>
    </view>

    <!-- 附件 -->
    <view v-if="(ticket.attachments || []).length" class="card">
      <view class="block-title">现场图片</view>
      <view class="img-grid">
        <image
          v-for="a in ticket.attachments"
          :key="a.attachmentId"
          :src="a.fileUrl"
          mode="aspectFill"
          class="img"
          @tap="preview(ticket.attachments.map((x: any) => x.fileUrl), a.fileUrl)"
        />
      </view>
    </view>

    <!-- 现场记录 -->
    <view v-if="(ticket.fieldRecords || []).length" class="card">
      <view class="block-title">现场记录</view>
      <view v-for="r in ticket.fieldRecords" :key="r.recordId" class="record">
        <view class="record-head">
          <text class="record-user">{{ r.createBy }}</text>
          <text class="muted">{{ r.recordTime }}</text>
        </view>
        <view class="record-addr">{{ r.address }}（{{ r.longitude }}, {{ r.latitude }}）</view>
        <view class="pre">{{ r.content }}</view>
        <view v-if="r.images?.length" class="img-grid">
          <image
            v-for="img in r.images"
            :key="img.attachmentId"
            :src="img.fileUrl"
            mode="aspectFill"
            class="img"
            @tap="preview(r.images.map((x: any) => x.fileUrl), img.fileUrl)"
          />
        </view>
      </view>
    </view>

    <!-- 时间线 -->
    <view class="card">
      <view class="block-title">处理时间线</view>
      <view class="timeline">
        <view v-for="(line, i) in ticket.timelines" :key="line.timelineId" class="t-item">
          <view class="t-dot" :class="{ first: line.action === 'CREATE' }"></view>
          <view v-if="i < ticket.timelines.length - 1" class="t-line"></view>
          <view class="t-body">
            <view class="t-head">
              <text class="t-action">{{ line.actionName }}</text>
              <text class="muted">{{ line.operateName }} · {{ line.createTime }}</text>
            </view>
            <view class="t-content">{{ line.content }}</view>
            <view v-if="line.attachments?.length" class="img-grid">
              <image
                v-for="img in line.attachments"
                :key="img.attachmentId"
                :src="img.fileUrl"
                mode="aspectFill"
                class="img small"
                @tap="preview(line.attachments.map((x: any) => x.fileUrl), img.fileUrl)"
              />
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作弹层 -->
    <view v-if="popup" class="mask-layer" @tap="popup = ''">
      <view class="sheet" @tap.stop>
        <view class="sheet-title">{{ actionTitle }}</view>

        <view v-if="['ASSIGN', 'TRANSFER'].includes(popup)" class="sheet-body">
          <view class="muted" style="margin-bottom: 12rpx">选择人员</view>
          <scroll-view scroll-y style="max-height: 480rpx">
            <view
              v-for="u in userOptions"
              :key="u.userId"
              class="user-row"
              :class="{ active: actionForm.targetUserId === Number(u.userId) }"
              @tap="actionForm.targetUserId = Number(u.userId)"
            >
              {{ u.realName }}（{{ u.username }}）
            </view>
          </scroll-view>
        </view>

        <view v-if="popup === 'COLLABORATE'" class="sheet-body">
          <scroll-view scroll-y style="max-height: 480rpx">
            <view
              v-for="u in userOptions"
              :key="u.userId"
              class="user-row"
              :class="{ active: actionForm.collaboratorIds.includes(Number(u.userId)) }"
              @tap="toggleCollaborator(Number(u.userId))"
            >
              {{ u.realName }}（{{ u.username }}）
            </view>
          </scroll-view>
        </view>

        <view v-if="popup === 'TRANSFER'" class="sheet-body">
          <textarea v-model="actionForm.content" class="ta" placeholder="转派原因（可选）" />
        </view>

        <view v-if="popup === 'HANDLE'" class="sheet-body">
          <textarea v-model="actionForm.content" class="ta" placeholder="请填写本次处理情况" />
        </view>

        <view v-if="popup === 'SUSPEND'" class="sheet-body">
          <picker :range="suspendDict" range-key="dictLabel" @change="onSuspendReason">
            <view class="picker-box">
              {{ suspendLabel || '请选择挂起原因' }}
            </view>
          </picker>
          <textarea v-model="actionForm.content" class="ta" placeholder="补充说明（可选）" />
        </view>

        <view v-if="popup === 'RESOLVE'" class="sheet-body">
          <textarea v-model="actionForm.solution" class="ta" placeholder="请填写最终解决方案" />
        </view>

        <view v-if="popup === 'CLOSE'" class="sheet-body">
          <view class="muted" style="margin-bottom: 12rpx">满意度评价（可选）</view>
          <view class="rate">
            <text
              v-for="n in 5"
              :key="n"
              class="star"
              :class="{ on: actionForm.satisfaction >= n }"
              @tap="actionForm.satisfaction = n"
            >
              ★
            </text>
          </view>
          <textarea v-model="actionForm.satisfactionComment" class="ta" placeholder="评价说明（可选）" />
        </view>

        <view v-if="['REOPEN', 'CANCEL'].includes(popup)" class="sheet-body">
          <textarea v-model="actionForm.content" class="ta" :placeholder="popup === 'CANCEL' ? '取消原因（可选）' : '重开原因（可选）'" />
        </view>

        <view v-if="popup === 'MERGE'" class="sheet-body">
          <input v-model.number="actionForm.targetTicketId" class="inp" placeholder="目标工单 ID" type="number" />
          <view class="muted" style="margin-top: 8rpx">在工单详情页顶部可查看工单 ID</view>
        </view>

        <view class="sheet-footer">
          <button class="btn-cancel" @tap="popup = ''">取消</button>
          <button class="btn-ok" :loading="submitting" @tap="submitAction">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { ticketApi } from '@/api'
import { useUserStore } from '@/store/user'
import { loadDict, statusColor, type DictItem } from '@/utils/dict'

const userStore = useUserStore()
const ticketId = ref('')
const ticket = ref<any>({})

let statusDict: DictItem[] = []
let priorityDict: DictItem[] = []
let sourceDict: DictItem[] = []
let suspendDict: DictItem[] = []

const ALLOWED: Record<string, string[]> = {
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
const PERMS: Record<string, string> = {
  ASSIGN: 'ticket:assign',
  TRANSFER: 'ticket:transfer',
  COLLABORATE: 'ticket:collaborate',
  ACCEPT: 'ticket:accept',
  HANDLE: 'ticket:handle',
  SUSPEND: 'ticket:suspend',
  RESUME: 'ticket:suspend',
  RESOLVE: 'ticket:resolve',
  CLOSE: 'ticket:close',
  REOPEN: 'ticket:reopen',
  CANCEL: 'ticket:cancel',
  MERGE: 'ticket:merge'
}
function can(action: string) {
  return (ALLOWED[action] || []).includes(ticket.value.status) && userStore.hasPermi(PERMS[action])
}

function statusLabel(s: string) {
  return statusDict.find((d) => d.dictValue === s)?.dictLabel || s
}
function priorityLabel(p: number) {
  return priorityDict.find((d) => Number(d.dictValue) === p)?.dictLabel || ''
}
function sourceLabel(s: string) {
  return sourceDict.find((d) => d.dictValue === s)?.dictLabel || ''
}

async function load() {
  uni.showLoading({ title: '加载中' })
  try {
    ticket.value = await ticketApi.detail(ticketId.value)
    actionForm.collaboratorIds = (ticket.value.collaborators || []).map((c: any) => Number(c.userId))
  } finally {
    uni.hideLoading()
  }
}

function preview(urls: string[], current: string) {
  uni.previewImage({ urls, current })
}

function goField() {
  uni.navigateTo({ url: `/pages/field/record?id=${ticketId.value}` })
}

// ---------------- 动作 ----------------
const popup = ref('')
const submitting = ref(false)
const userOptions = ref<any[]>([])
const actionForm = reactive<any>({
  targetUserId: undefined,
  collaboratorIds: [] as number[],
  content: '',
  solution: '',
  suspendReason: '',
  satisfaction: 0,
  satisfactionComment: '',
  targetTicketId: undefined
})

const TITLES: Record<string, string> = {
  ASSIGN: '工单指派',
  TRANSFER: '工单转派',
  COLLABORATE: '设置协同人',
  HANDLE: '提交处理',
  SUSPEND: '挂起工单',
  RESOLVE: '解决工单',
  CLOSE: '关闭工单',
  REOPEN: '重开工单',
  CANCEL: '取消工单',
  MERGE: '合并工单'
}
const actionTitle = computed(() => TITLES[popup.value] || '')
const suspendLabel = computed(() => suspendDict.find((d) => d.dictValue === actionForm.suspendReason)?.dictLabel)

async function openAction(type: string) {
  Object.assign(actionForm, {
    targetUserId: undefined,
    collaboratorIds: (ticket.value.collaborators || []).map((c: any) => Number(c.userId)),
    content: '',
    solution: '',
    suspendReason: '',
    satisfaction: 0,
    satisfactionComment: '',
    targetTicketId: undefined
  })
  popup.value = type
  if (['ASSIGN', 'TRANSFER', 'COLLABORATE'].includes(type) && userOptions.value.length === 0) {
    userOptions.value = await ticketApi.userOptions()
  }
}
function toggleCollaborator(uid: number) {
  const i = actionForm.collaboratorIds.indexOf(uid)
  if (i >= 0) actionForm.collaboratorIds.splice(i, 1)
  else actionForm.collaboratorIds.push(uid)
}
function onSuspendReason(e: any) {
  actionForm.suspendReason = suspendDict[Number(e.detail.value)].dictValue
}

async function confirmAction(type: string) {
  const res = await uni.showModal({ title: '提示', content: `确定执行「${TITLES[type]}」操作吗？` })
  if (res.confirm) {
    await doSubmit(type)
  }
}

async function submitAction() {
  const t = popup.value
  if (['ASSIGN', 'TRANSFER'].includes(t) && !actionForm.targetUserId) {
    uni.showToast({ title: '请选择人员', icon: 'none' })
    return
  }
  if (t === 'COLLABORATE' && !actionForm.collaboratorIds.length) {
    uni.showToast({ title: '请选择协同人', icon: 'none' })
    return
  }
  if (t === 'HANDLE' && !actionForm.content.trim()) {
    uni.showToast({ title: '请填写处理情况', icon: 'none' })
    return
  }
  if (t === 'SUSPEND' && !actionForm.suspendReason && !actionForm.content.trim()) {
    uni.showToast({ title: '请选择或填写挂起原因', icon: 'none' })
    return
  }
  if (t === 'RESOLVE' && !actionForm.solution.trim()) {
    uni.showToast({ title: '请填写解决方案', icon: 'none' })
    return
  }
  if (t === 'MERGE' && !actionForm.targetTicketId) {
    uni.showToast({ title: '请填写目标工单 ID', icon: 'none' })
    return
  }
  await doSubmit(t)
}

async function doSubmit(type: string) {
  submitting.value = true
  try {
    await ticketApi.action(type.toLowerCase(), {
      ticketId: Number(ticketId.value),
      ...JSON.parse(JSON.stringify(actionForm)),
      satisfaction: actionForm.satisfaction > 0 ? actionForm.satisfaction : undefined
    })
    uni.showToast({ title: '操作成功', icon: 'success' })
    popup.value = ''
    load()
  } finally {
    submitting.value = false
  }
}

onLoad(async (opts) => {
  ticketId.value = opts?.id || ''
  statusDict = await loadDict('ticket_status')
  priorityDict = await loadDict('ticket_priority')
  sourceDict = await loadDict('ticket_source')
  suspendDict = await loadDict('suspend_reason')
  load()
})
</script>

<style lang="scss" scoped>
.page {
  padding-bottom: 40rpx;
}
.head {
  padding: 36rpx 32rpx;
  color: #fff;
}
.head-row {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  opacity: 0.9;
}
.head-title {
  font-size: 38rpx;
  font-weight: 700;
  margin: 16rpx 0 10rpx;
}
.head-sub {
  font-size: 24rpx;
  opacity: 0.9;
}
.action-bar {
  white-space: nowrap;
  background: #fff;
  padding: 16rpx 20rpx;
}
.action-inner {
  display: inline-flex;
  gap: 14rpx;
}
.act {
  display: inline-block;
  font-size: 26rpx;
  padding: 0 28rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 32rpx;
  background: #f4f5f7;
  color: #606266;
  margin: 0;
}
.act::after {
  border: none;
}
.act-primary {
  background: #2b6cb0;
  color: #fff;
}
.act-success {
  background: #67c23a;
  color: #fff;
}
.act-warning {
  background: #e6a23c;
  color: #fff;
}
.act-danger {
  background: #f56c6c;
  color: #fff;
}
.info-row {
  display: flex;
  padding: 14rpx 0;
  font-size: 28rpx;
}
.k {
  width: 140rpx;
  color: #909399;
  flex-shrink: 0;
}
.v {
  flex: 1;
}
.block-title {
  font-weight: 600;
  margin: 20rpx 0 10rpx;
}
.pre {
  background: #f7f8fa;
  border-radius: 10rpx;
  padding: 20rpx;
  font-size: 28rpx;
  line-height: 1.7;
  white-space: pre-wrap;
}
.success-pre {
  background: #f0f9eb;
}
.img-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 14rpx;
}
.img {
  width: 210rpx;
  height: 210rpx;
  border-radius: 10rpx;
}
.img.small {
  width: 150rpx;
  height: 150rpx;
}
.record {
  padding: 20rpx 0;
  border-bottom: 1rpx dashed #ebeef5;
}
.record:last-child {
  border-bottom: none;
}
.record-head {
  display: flex;
  justify-content: space-between;
}
.record-user {
  font-weight: 600;
  color: #2b6cb0;
}
.record-addr {
  font-size: 26rpx;
  margin: 8rpx 0;
}
.timeline {
  padding-left: 10rpx;
}
.t-item {
  position: relative;
  padding-left: 36rpx;
}
.t-dot {
  position: absolute;
  left: 0;
  top: 10rpx;
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
  background: #c0c4cc;
}
.t-dot.first {
  background: #2b6cb0;
}
.t-line {
  position: absolute;
  left: 8rpx;
  top: 30rpx;
  bottom: -10rpx;
  width: 2rpx;
  background: #e4e7ed;
}
.t-head {
  display: flex;
  gap: 14rpx;
  align-items: center;
}
.t-action {
  font-weight: 600;
}
.t-content {
  margin: 6rpx 0;
  font-size: 26rpx;
  color: #606266;
}
.mask-layer {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}
.sheet {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx 32rpx calc(30rpx + env(safe-area-inset-bottom));
}
.sheet-title {
  text-align: center;
  font-weight: 600;
  font-size: 32rpx;
  margin-bottom: 24rpx;
}
.user-row {
  padding: 22rpx 20rpx;
  border-radius: 10rpx;
  margin-bottom: 8rpx;
  background: #f7f8fa;
}
.user-row.active {
  background: rgba(43, 108, 176, 0.12);
  color: #2b6cb0;
  font-weight: 600;
}
.ta {
  width: 100%;
  box-sizing: border-box;
  min-height: 200rpx;
  background: #f7f8fa;
  border-radius: 10rpx;
  padding: 20rpx;
  margin-top: 16rpx;
}
.inp {
  width: 100%;
  box-sizing: border-box;
  height: 80rpx;
  background: #f7f8fa;
  border-radius: 10rpx;
  padding: 0 20rpx;
}
.picker-box {
  background: #f7f8fa;
  border-radius: 10rpx;
  padding: 22rpx 20rpx;
}
.rate {
  margin-bottom: 16rpx;
}
.star {
  font-size: 56rpx;
  color: #dcdfe6;
  margin-right: 12rpx;
}
.star.on {
  color: #f7ba2a;
}
.sheet-footer {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}
.btn-cancel,
.btn-ok {
  flex: 1;
  border-radius: 12rpx;
  font-size: 30rpx;
}
.btn-cancel {
  background: #f4f5f7;
  color: #606266;
}
.btn-cancel::after {
  border: none;
}
.btn-ok {
  background: #2b6cb0;
  color: #fff;
}
.btn-ok::after {
  border: none;
}
.muted {
  color: #909399;
  font-size: 24rpx;
}
</style>
