import service from '@/utils/request'

/** 通用文件上传, 返回 UploadResult */
export function uploadFile(file: File | Blob, filename?: string) {
  const form = new FormData()
  const realName = filename || (file as File).name || 'image.jpg'
  form.append('file', file, realName)
  return service.post('/api/file/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }) as Promise<any>
}
