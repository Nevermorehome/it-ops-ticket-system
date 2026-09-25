import { get, post, put } from '@/utils/request'

export interface TicketQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  categoryId?: string | number
  status?: string
  priority?: number
  source?: string
  deptId?: string | number
  handlerId?: string | number
  reporterId?: string | number
  beginTime?: string
  endTime?: string
  dimension?: string
}

export function pageTicket(params: TicketQuery) {
  return get('/api/ticket/page', params)
}
export function getTicket(id: string | number) {
  return get(`/api/ticket/${id}`)
}
export function createTicket(ticket: any, attachments: any[] = []) {
  return post('/api/ticket', { ticket, attachments })
}
export function updateTicket(ticket: any) {
  return put('/api/ticket', ticket)
}

/** 工单状态机动作 */
export function doAction(action: string, data: any) {
  return put(`/api/ticket/${action}`, data)
}
export function addFieldRecord(ticketId: string | number, record: any, images: any[]) {
  return post(`/api/ticket/${ticketId}/field-record`, { record, images })
}
export function listFieldRecord(ticketId: string | number) {
  return get(`/api/ticket/${ticketId}/field-record/list`)
}
export function userOptions(keyword?: string) {
  return get('/api/system/user/options', { keyword })
}
