import type { Directive } from 'vue'
import { useUserStore } from '@/store/user'

/** v-hasPermi="['ticket:assign']" 无权限移除元素 */
export const hasPermi: Directive = {
  mounted(el, binding) {
    const store = useUserStore()
    const perms: string[] = binding.value || []
    const ok = perms.length === 0 || perms.some((p) => store.hasPermi(p))
    if (!ok && el.parentNode) {
      el.parentNode.removeChild(el)
    }
  }
}

/** v-hasRole="['admin']" */
export const hasRole: Directive = {
  mounted(el, binding) {
    const store = useUserStore()
    const roles: string[] = binding.value || []
    const ok = roles.length === 0 || roles.some((r) => store.roles.includes(r))
    if (!ok && el.parentNode) {
      el.parentNode.removeChild(el)
    }
  }
}
