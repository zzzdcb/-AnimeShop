import { ElMessageBox } from 'element-plus'

/**
 * 显示模块对话框
 * @param content 显示内容
 * @param title 显示标题
 * @param type 显示类型
 */
export function showModule(
  content: string,
  title: string,
  type: 'success' | 'warning' | 'info' | 'error'
) {
  return ElMessageBox.confirm(content, title, {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type
  })
}
