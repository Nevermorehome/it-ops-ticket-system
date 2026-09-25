<template>
  <div class="app-container">
    <el-form class="search-form" :inline="true" @submit.prevent>
      <el-form-item label="地点名称">
        <el-input v-model="query.name" placeholder="名称/楼栋" clearable style="width: 200px" @keyup.enter="load" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
      </el-form-item>
    </el-form>
    <div class="table-card">
      <div class="toolbar">
        <el-button v-hasPermi="['base:location:add']" type="primary" :icon="Plus" @click="openDialog()">
          新增地点
        </el-button>
      </div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="locationName" label="地点名称" min-width="140" />
        <el-table-column prop="building" label="楼栋" width="120" />
        <el-table-column prop="floor" label="楼层" width="90" />
        <el-table-column prop="room" label="房间" width="100" />
        <el-table-column prop="address" label="详细地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="经纬度" width="200">
          <template #default="{ row }">
            <span v-if="row.longitude" class="mono">{{ row.longitude }}, {{ row.latitude }}</span>
            <span v-else>—</span>
          </template>
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
            <el-button v-hasPermi="['base:location:edit']" link type="primary" size="small" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['base:location:remove']" link type="danger" size="small" @click="remove(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.locationId ? '编辑地点' : '新增地点'" width="520px">
      <el-form :model="form" label-width="84px">
        <el-form-item label="地点名称" required>
          <el-input v-model="form.locationName" placeholder="如：总部办公楼" />
        </el-form-item>
        <el-row>
          <el-col :span="8">
            <el-form-item label="楼栋">
              <el-input v-model="form.building" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="楼层">
              <el-input v-model="form.floor" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="房间">
              <el-input v-model="form.room" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" placeholder="道路门牌等" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input v-model.number="form.longitude" placeholder="116.397428" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input v-model.number="form.latitude" placeholder="39.90923" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { onMounted, reactive, ref } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { locationApi } from '@/api/base'

const loading = ref(false)
const list = ref<any[]>([])
const query = reactive({ name: '' })
const visible = ref(false)
const form = reactive<any>({
  locationId: undefined,
  locationName: '',
  building: '',
  floor: '',
  room: '',
  address: '',
  longitude: undefined,
  latitude: undefined,
  status: '0',
  remark: ''
})

async function load() {
  loading.value = true
  try {
    const res = await locationApi.list({ name: query.name || undefined })
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  if (row) {
    Object.assign(form, {
      ...row,
      longitude: row.longitude ? Number(row.longitude) : undefined,
      latitude: row.latitude ? Number(row.latitude) : undefined
    })
  } else {
    Object.assign(form, {
      locationId: undefined,
      locationName: '',
      building: '',
      floor: '',
      room: '',
      address: '',
      longitude: undefined,
      latitude: undefined,
      status: '0',
      remark: ''
    })
  }
  visible.value = true
}

async function submit() {
  if (!form.locationName?.trim()) {
    ElMessage.warning('请输入地点名称')
    return
  }
  if (form.locationId) {
    await locationApi.update({ ...form })
  } else {
    await locationApi.create({ ...form })
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除地点「${row.locationName}」吗？`, '提示', { type: 'warning' })
  await locationApi.remove(row.locationId)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
