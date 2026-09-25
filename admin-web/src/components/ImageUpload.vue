<template>
  <el-upload
    :file-list="fileList"
    list-type="picture-card"
    :limit="limit"
    accept="image/*"
    multiple
    :before-upload="beforeUpload"
    :http-request="customUpload"
    :on-remove="handleRemove"
    :on-preview="handlePreview"
  >
    <el-icon :size="22"><Plus /></el-icon>
    <template #tip>
      <div class="tip">支持 jpg/png/webp，单张不超过 10MB，最多 {{ limit }} 张</div>
    </template>
  </el-upload>
  <el-dialog v-model="previewVisible" title="预览" width="600px" append-to-body>
    <img :src="previewUrl" alt="预览" class="preview-img" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import type { UploadFile, UploadFiles, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/file'

interface Attachment {
  fileName?: string
  fileUrl: string
  fileSize?: number
  fileType?: string
}

const props = withDefaults(defineProps<{ modelValue: Attachment[]; limit?: number }>(), {
  modelValue: () => [],
  limit: 9
})
const emit = defineEmits<{ (e: 'update:modelValue', v: Attachment[]): void }>()

const fileList = ref<UploadFiles>([])
const previewVisible = ref(false)
const previewUrl = ref('')

watch(
  () => props.modelValue,
  (list) => {
    fileList.value = (list || []).map((a, i) => ({
      name: a.fileName || `image-${i + 1}`,
      url: a.fileUrl,
      status: 'success'
    })) as UploadFiles
  },
  { immediate: true }
)

function beforeUpload(file: File) {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片不能超过 10MB')
    return false
  }
  return true
}

async function customUpload(options: UploadRequestOptions) {
  const file = options.file as File
  try {
    const res: any = await uploadFile(file)
    const result = res.data
    emit('update:modelValue', [
      ...props.modelValue,
      {
        fileName: result.fileName,
        fileUrl: result.url,
        fileSize: result.size,
        fileType: result.fileType
      }
    ])
  } catch {
    ElMessage.error(`${file.name} 上传失败`)
  }
}

function handleRemove(_file: UploadFile, files: UploadFiles) {
  const urls = files.map((f) => f.url)
  emit(
    'update:modelValue',
    props.modelValue.filter((a) => urls.includes(a.fileUrl))
  )
}

function handlePreview(file: UploadFile) {
  previewUrl.value = file.url!
  previewVisible.value = true
}
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.preview-img {
  width: 100%;
}
</style>
