<template>
  <div class="app-container">
    <el-form class="search-form" :inline="true" @submit.prevent>
      <el-form-item label="参数名称">
        <el-input v-model="query.configName" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="参数键名">
        <el-input v-model="query.configKey" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <div class="table-card">
      <div class="toolbar">
        <el-button v-hasPermi="['system:config:add']" type="primary" :icon="Plus" @click="openDialog()">
          新增参数
        </el-button>
      </div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="configName" label="参数名称" width="200" />
        <el-table-column prop="configKey" label="参数键名" width="240" class-name="mono" />
        <el-table-column prop="configValue" label="参数值" min-width="200" show-overflow-tooltip />
        <el-table-column label="内置" width="80">
          <template #default="{ row }">
            <el-tag :type="row.configType === 'Y' ? 'success' : 'info'" size="small">
              {{ row.configType === 'Y' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button v-hasPermi="['system:config:edit']" link type="primary" size="small" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['system:config:remove']" link type="danger" size="small" @click="remove(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.configId ? '编辑参数' : '新增参数'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="参数名称" required>
          <el-input v-model="form.configName" />
        </el-form-item>
        <el-form-item label="参数键名" required>
          <el-input v-model="form.configKey" class="mono" />
        </el-form-item>
        <el-form-item label="参数值" required>
          <el-input v-model="form.configValue" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="系统内置">
          <el-radio-group v-model="form.configType">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
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
import { onMounted, reactive, ref } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { configApi } from '@/api/system'

const loading = ref(false)
const list = ref<any[]>([])
const query = reactive({ configName: '', configKey: '' })
const visible = ref(false)
const form = reactive<any>({ configId: undefined, configName: '', configKey: '', configValue: '', configType: 'N', remark: '' })

async function load() {
  loading.value = true
  try {
    const res = await configApi.list({
      configName: query.configName || undefined,
      configKey: query.configKey || undefined
    })
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { configId: undefined, configName: '', configKey: '', configValue: '', configType: 'N', remark: '' })
  }
  visible.value = true
}

async function submit() {
  if (!form.configName || !form.configKey || form.configValue === '') {
    ElMessage.warning('请填写完整')
    return
  }
  if (form.configId) {
    await configApi.update({ ...form })
  } else {
    await configApi.create({ ...form })
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除参数「${row.configName}」吗？`, '提示', { type: 'warning' })
  await configApi.remove(row.configId)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
