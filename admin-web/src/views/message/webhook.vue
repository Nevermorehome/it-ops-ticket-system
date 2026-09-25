<template>
  <div class="app-container">
    <el-alert
      type="info"
      :closable="false"
      style="margin-bottom: 12px"
      title="配置企业微信 / 钉钉 / 飞书群机器人 Webhook，工单关键动作将自动推送到群。钉钉加签机器人需填写 Secret。"
    />
    <div class="table-card">
      <div class="toolbar">
        <el-button v-hasPermi="['message:webhook:list']" type="primary" :icon="Plus" @click="openDialog()">
          新增配置
        </el-button>
      </div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="name" label="名称" width="160" />
        <el-table-column label="平台" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="webhookUrl" label="Webhook 地址" min-width="280" show-overflow-tooltip />
        <el-table-column prop="events" label="订阅动作" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.events === '*' ? '全部动作' : row.events || '全部动作' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'" size="small">
              {{ row.status === '0' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button v-hasPermi="['message:webhook:list']" link type="primary" size="small" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['message:webhook:list']" link type="danger" size="small" @click="remove(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.webhookId ? '编辑配置' : '新增配置'" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="如：运维告警群" />
        </el-form-item>
        <el-form-item label="平台类型" required>
          <el-radio-group v-model="form.type">
            <el-radio value="wechat">企业微信</el-radio>
            <el-radio value="dingtalk">钉钉</el-radio>
            <el-radio value="feishu">飞书</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Webhook 地址" required>
          <el-input v-model="form.webhookUrl" placeholder="https://..." type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="加签 Secret">
          <el-input v-model="form.secret" placeholder="钉钉加签机器人填写(SEC 开头)" />
        </el-form-item>
        <el-form-item label="订阅动作">
          <el-select
            v-model="eventList"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="留空=全部动作"
            style="width: 100%"
          >
            <el-option
              v-for="e in ACTIONS"
              :key="e.value"
              :label="`${e.label} (${e.value})`"
              :value="e.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { webhookApi } from '@/api/message'

const ACTIONS = [
  { label: '创建', value: 'CREATE' },
  { label: '指派', value: 'ASSIGN' },
  { label: '转派', value: 'TRANSFER' },
  { label: '受理', value: 'ACCEPT' },
  { label: '处理', value: 'HANDLE' },
  { label: '挂起', value: 'SUSPEND' },
  { label: '恢复', value: 'RESUME' },
  { label: '解决', value: 'RESOLVE' },
  { label: '关闭', value: 'CLOSE' },
  { label: '重开', value: 'REOPEN' },
  { label: '取消', value: 'CANCEL' },
  { label: '合并', value: 'MERGE' }
]

const loading = ref(false)
const list = ref<any[]>([])
const visible = ref(false)
const form = reactive<any>({
  webhookId: undefined,
  name: '',
  type: 'wechat',
  webhookUrl: '',
  secret: '',
  events: '',
  status: '0',
  remark: ''
})
const eventList = computed<string[]>({
  get: () => (form.events && form.events !== '*' ? form.events.split(',').filter(Boolean) : []),
  set: (v) => (form.events = v.length ? v.join(',') : '*')
})

function typeLabel(t: string) {
  return { wechat: '企业微信', dingtalk: '钉钉', feishu: '飞书' }[t] || t
}
function typeTag(t: string): any {
  return ({ wechat: 'success', dingtalk: 'warning', feishu: 'primary' } as any)[t] || 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await webhookApi.list()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, {
      webhookId: undefined,
      name: '',
      type: 'wechat',
      webhookUrl: '',
      secret: '',
      events: '*',
      status: '0',
      remark: ''
    })
  }
  visible.value = true
}

async function submit() {
  if (!form.name || !form.webhookUrl) {
    ElMessage.warning('请填写名称与 Webhook 地址')
    return
  }
  if (form.webhookId) {
    await webhookApi.update({ ...form })
  } else {
    await webhookApi.create({ ...form })
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除配置「${row.name}」吗？`, '提示', { type: 'warning' })
  await webhookApi.remove(row.webhookId)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
