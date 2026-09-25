import { dictApi } from '@/api/system'

export interface DictItem {
  dictLabel: string
  dictValue: string
  cssClass?: string
  listClass?: string
}

const cache = new Map<string, DictItem[]>()

/** 字典数据加载与缓存 */
export async function loadDict(dictType: string): Promise<DictItem[]> {
  if (cache.has(dictType)) {
    return cache.get(dictType)!
  }
  const res = await dictApi.dataByType(dictType)
  const list: DictItem[] = res.data || []
  cache.set(dictType, list)
  return list
}

export function clearDictCache() {
  cache.clear()
}

/** 优先级/来源 等固定字典的本地兜底(字典接口不可用时仍可渲染) */
export const FALLBACK: Record<string, DictItem[]> = {
  ticket_priority: [
    { dictLabel: '低', dictValue: '0', listClass: 'info' },
    { dictLabel: '中', dictValue: '1', listClass: '' },
    { dictLabel: '高', dictValue: '2', listClass: 'warning' },
    { dictLabel: '紧急', dictValue: '3', listClass: 'danger' }
  ],
  ticket_source: [
    { dictLabel: '电话报修', dictValue: 'phone' },
    { dictLabel: '微信/企微', dictValue: 'wechat' },
    { dictLabel: '现场报障', dictValue: 'onsite' },
    { dictLabel: '自助提单', dictValue: 'self' },
    { dictLabel: '其他', dictValue: 'other' }
  ],
  ticket_status: [
    { dictLabel: '待受理', dictValue: 'PENDING', listClass: 'info' },
    { dictLabel: '已指派', dictValue: 'ASSIGNED', listClass: '' },
    { dictLabel: '处理中', dictValue: 'PROCESSING', listClass: 'primary' },
    { dictLabel: '已挂起', dictValue: 'SUSPENDED', listClass: 'warning' },
    { dictLabel: '已解决', dictValue: 'RESOLVED', listClass: 'success' },
    { dictLabel: '已关闭', dictValue: 'CLOSED', listClass: 'info' },
    { dictLabel: '已取消', dictValue: 'CANCELLED', listClass: 'danger' },
    { dictLabel: '已合并', dictValue: 'MERGED', listClass: 'info' }
  ],
  suspend_reason: [
    { dictLabel: '等待用户确认', dictValue: 'wait_user' },
    { dictLabel: '等待配件', dictValue: 'wait_part' },
    { dictLabel: '等待第三方', dictValue: 'wait_vendor' },
    { dictLabel: '其他', dictValue: 'other' }
  ]
}

export async function useDictData(dictType: string): Promise<DictItem[]> {
  try {
    const list = await loadDict(dictType)
    return list.length ? list : FALLBACK[dictType] || []
  } catch {
    return FALLBACK[dictType] || []
  }
}

export function fallbackDict(dictType: string): DictItem[] {
  return FALLBACK[dictType] || []
}
