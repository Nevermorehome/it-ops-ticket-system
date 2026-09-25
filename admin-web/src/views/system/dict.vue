<template>
  <div class="app-container">
    <el-row :gutter="14">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-head">
              <span>字典类型</span>
              <el-button v-hasPermi="['system:dict:add']" type="primary" size="small" :icon="Plus" @click="openTypeDialog()">
                新增
              </el-button>
            </div>
          </template>
          <el-form :inline="true" @submit.prevent style="margin-bottom: 8px">
            <el-form-item>
              <el-input v-model="typeQuery.dictName" placeholder="字典名称" clearable style="width: 130px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="loadTypes">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table
            v-loading="typeLoading"
            :data="typeList"
            border
            size="small"
            highlight-current-row
            @row-click="selectType"
          >
            <el-table-column prop="dictName" label="名称" width="110" />
            <el-table-column prop="dictType" label="类型编码" min-width="130">
              <template #default="{ row }">
                <span class="mono" :class="{ active: current?.dictId === row.dictId }">{{ row.dictType }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button v-hasPermi="['system:dict:edit']" link type="primary" size="small" @click.stop="openTypeDialog(row)">
                  修改
                </el-button>
                <el-button v-hasPermi="['system:dict:remove']" link type="danger" size="small" @click.stop="removeType(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-head">
              <span>字典数据<template v-if="current"> - {{ current.dictName }}</template></span>
              <el-button
                v-hasPermi="['system:dict:add']"
                type="primary"
                size="small"
                :icon="Plus"
                :disabled="!current"
                @click="openDataDialog()"
              >
                新增
              </el-button>
            </div>
          </template>
          <el-table v-loading="dataLoading" :data="dataList" border size="small">
            <el-table-column prop="dictLabel" label="标签" width="110" />
            <el-table-column prop="dictValue" label="键值" width="110" />
            <el-table-column label="样式" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.listClass" :type="row.listClass" size="small">{{ row.listClass }}</el-tag>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column prop="orderNum" label="排序" width="60" />
            <el-table-column label="状态" width="70">
              <template #default="{ row }">
                <el-tag :type="row.status === '0' ? 'success' : 'info'" size="small">
                  {{ row.status === '0' ? '正常' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button v-hasPermi="['system:dict:edit']" link type="primary" size="small" @click="openDataDialog(row)">
                  修改
                </el-button>
                <el-button v-hasPermi="['system:dict:remove']" link type="danger" size="small" @click="removeData(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 字典类型弹窗 -->
    <el-dialog v-model="typeVisible" :title="typeForm.dictId ? '编辑字典类型' : '新增字典类型'" width="440px">
      <el-form :model="typeForm" label-width="84px">
        <el-form-item label="字典名称" required>
          <el-input v-model="typeForm.dictName" />
        </el-form-item>
        <el-form-item label="类型编码" required>
          <el-input v-model="typeForm.dictType" placeholder="如 ticket_status" class="mono" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitType">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog v-model="dataVisible" :title="dataForm.dictCode ? '编辑字典项' : '新增字典项'" width="440px">
      <el-form :model="dataForm" label-width="84px">
        <el-form-item label="字典标签" required>
          <el-input v-model="dataForm.dictLabel" />
        </el-form-item>
        <el-form-item label="字典键值" required>
          <el-input v-model="dataForm.dictValue" />
        </el-form-item>
        <el-form-item label="标签样式">
          <el-select v-model="dataForm.listClass" clearable style="width: 100%">
            <el-option label="默认" value="" />
            <el-option label="primary" value="primary" />
            <el-option label="success" value="success" />
            <el-option label="warning" value="warning" />
            <el-option label="danger" value="danger" />
            <el-option label="info" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataForm.orderNum" :min="0" :max="99" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataVisible = false">取消</el-button>
        <el-button type="primary" @click="submitData">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dictApi } from '@/api/system'
import { clearDictCache } from '@/composables/useDict'

const typeLoading = ref(false)
const dataLoading = ref(false)
const typeList = ref<any[]>([])
const dataList = ref<any[]>([])
const current = ref<any>(null)
const typeQuery = reactive({ dictName: '' })

const typeVisible = ref(false)
const typeForm = reactive<any>({ dictId: undefined, dictName: '', dictType: '', status: '0', remark: '' })
const dataVisible = ref(false)
const dataForm = reactive<any>({
  dictCode: undefined,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  listClass: '',
  orderNum: 0,
  status: '0',
  remark: ''
})

async function loadTypes() {
  typeLoading.value = true
  try {
    const res = await dictApi.typeList({ dictName: typeQuery.dictName || undefined })
    typeList.value = res.data || []
  } finally {
    typeLoading.value = false
  }
}

async function selectType(row: any) {
  current.value = row
  dataLoading.value = true
  try {
    const res = await dictApi.dataList({ dictType: row.dictType })
    dataList.value = res.data || []
  } finally {
    dataLoading.value = false
  }
}

function openTypeDialog(row?: any) {
  if (row) {
    Object.assign(typeForm, row)
  } else {
    Object.assign(typeForm, { dictId: undefined, dictName: '', dictType: '', status: '0', remark: '' })
  }
  typeVisible.value = true
}
async function submitType() {
  if (!typeForm.dictName || !typeForm.dictType) {
    ElMessage.warning('名称和编码必填')
    return
  }
  if (typeForm.dictId) {
    await dictApi.typeUpdate({ ...typeForm })
  } else {
    await dictApi.typeCreate({ ...typeForm })
  }
  ElMessage.success('保存成功')
  typeVisible.value = false
  clearDictCache()
  loadTypes()
}
async function removeType(row: any) {
  await ElMessageBox.confirm(`删除字典类型「${row.dictName}」将同时移除其字典项，确定吗？`, '提示', {
    type: 'warning'
  })
  await dictApi.typeRemove(row.dictId)
  ElMessage.success('已删除')
  clearDictCache()
  if (current.value?.dictId === row.dictId) {
    current.value = null
    dataList.value = []
  }
  loadTypes()
}

function openDataDialog(row?: any) {
  if (row) {
    Object.assign(dataForm, row)
  } else {
    Object.assign(dataForm, {
      dictCode: undefined,
      dictType: current.value.dictType,
      dictLabel: '',
      dictValue: '',
      listClass: '',
      orderNum: 0,
      status: '0',
      remark: ''
    })
  }
  dataVisible.value = true
}
async function submitData() {
  if (!dataForm.dictLabel || !dataForm.dictValue) {
    ElMessage.warning('标签和键值必填')
    return
  }
  if (dataForm.dictCode) {
    await dictApi.dataUpdate({ ...dataForm })
  } else {
    await dictApi.dataCreate({ ...dataForm })
  }
  ElMessage.success('保存成功')
  dataVisible.value = false
  clearDictCache()
  selectType(current.value)
}
async function removeData(row: any) {
  await ElMessageBox.confirm(`确定删除字典项「${row.dictLabel}」吗？`, '提示', { type: 'warning' })
  await dictApi.dataRemove(row.dictCode)
  ElMessage.success('已删除')
  clearDictCache()
  selectType(current.value)
}

onMounted(loadTypes)
</script>

<style scoped>
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.active {
  color: #409eff;
  font-weight: 600;
}
</style>
