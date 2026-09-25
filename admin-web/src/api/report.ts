import { get } from '@/utils/request'
import { downloadFile } from '@/utils/request'

export const reportApi = {
  overview: (start?: string, end?: string) => get('/api/report/overview', { start, end }),
  trend: (start?: string, end?: string) => get('/api/report/trend', { start, end }),
  distribution: (groupBy: string, start?: string, end?: string) =>
    get('/api/report/distribution', { groupBy, start, end }),
  rank: (start?: string, end?: string) => get('/api/report/rank', { start, end }),
  dept: (start?: string, end?: string) => get('/api/report/dept', { start, end }),
  export: (type: string, start?: string, end?: string) =>
    downloadFile('/api/report/export', { type, start, end }, `IT运维工单统计.${type === 'excel' ? 'xlsx' : type}`)
}
