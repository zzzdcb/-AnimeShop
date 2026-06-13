<template>
  <div class="address-page">
    <div class="page-header">
      <h1 class="page-title">
        <span class="title-emoji">📍</span>
        我的地址
        <span class="title-emoji">🏠</span>
      </h1>
      <el-button type="primary" class="add-btn" @click="handleAdd">
        <span class="btn-emoji">➕</span>
        新增地址
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="anime-loading">
        <span class="loading-emoji">📍</span>
        <span class="loading-emoji">✨</span>
        <span>加载中...</span>
      </div>
    </div>

    <!-- 地址列表 -->
    <div class="address-list" v-else-if="addresses.length > 0">
      <div
        v-for="address in addresses"
        :key="address.id"
        class="address-card"
        :class="{ default: address.isDefault }"
      >
        <div class="address-info">
          <div class="address-header">
            <span class="receiver">{{ address.receiver }}</span>
            <span class="phone">{{ address.phone }}</span>
            <span v-if="address.isDefault" class="default-badge">默认</span>
          </div>
          <div class="address-detail">
            <span class="address-full">
              {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
            </span>
          </div>
        </div>
        <div class="address-actions">
          <el-button link @click="handleEdit(address)">
            <span class="action-emoji">✏️</span>
            编辑
          </el-button>
          <el-button link @click="handleDelete(address.id)">
            <span class="action-emoji">🗑️</span>
            删除
          </el-button>
          <el-button link v-if="!address.isDefault" @click="handleSetDefault(address.id)">
            <span class="action-emoji">⭐</span>
            设为默认
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-address">
      <div class="empty-anime">
        <span class="empty-emoji">😿</span>
        <span class="empty-emoji">📍</span>
        <span class="empty-emoji">🏠</span>
        <h3>还没有收货地址</h3>
        <p>添加一个地址，让商品更快送到你手中~</p>
        <el-button type="primary" class="add-empty-btn" @click="handleAdd">➕ 新增地址</el-button>
      </div>
    </div>

    <!-- 新增/编辑地址弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" class="address-dialog">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="formData.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="所在地区" prop="province">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-input v-model="formData.province" placeholder="省" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="formData.city" placeholder="市" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="formData.district" placeholder="区/县" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input
            v-model="formData.detail"
            type="textarea"
            rows="2"
            placeholder="请输入街道、门牌号等详细信息"
          />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="formData.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAddressService,
  deleteAddressService,
  addAddressService,
  putAddressService
} from '@/api/address'

// 地址类型
interface AddressItem {
  id: number
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: boolean
}

// 表单数据类型
interface FormDataType {
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: boolean
}

const addresses = ref<AddressItem[]>([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增地址')
const isEdit = ref(false)
const editId = ref(0)
const formRef = ref()

// 表单数据
const formData = reactive<FormDataType>({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

// 表单验证规则
const rules = {
  receiver: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区/县', trigger: 'blur' }],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 100, message: '长度在 5 到 100 个字符', trigger: 'blur' }
  ]
}

// 获取地址列表
const getAddress = async () => {
  loading.value = true
  try {
    const res = await getAddressService()
    console.log('完整响应:', res)

    // 后端返回格式：{ code: 200, data: [...] }
    const dataArray = res.data || []

    if (dataArray.length > 0) {
      addresses.value = dataArray.map((item: any) => ({
        id: item.id,
        receiver: item.receiver,
        phone: item.phone,
        province: item.province || '',
        city: item.city || '',
        district: item.district || '',
        detail: item.address || '',
        isDefault: item.isDefault === 1
      }))
    } else {
      addresses.value = []
    }

    console.log('处理后的地址列表:', addresses.value)
  } catch (error) {
    console.error('获取地址失败:', error)
    ElMessage.error('获取地址失败')
    addresses.value = []
  } finally {
    loading.value = false
  }
}

// 删除地址（本地立即删除）
const handleDelete = (addressId: number) => {
  ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    // 保存被删除的数据（用于回滚）
    const deletedItem = addresses.value.find((item) => item.id === addressId)
    const originalAddresses = [...addresses.value]

    // 立即从本地删除（使用 filter 返回新数组，触发响应式更新）
    addresses.value = addresses.value.filter((item) => item.id !== addressId)
    ElMessage.success('删除成功')

    try {
      await deleteAddressService(addressId)
      // 静默刷新，确保数据一致
      await getAddress()
    } catch (error) {
      console.error('删除失败:', error)
      // 失败时恢复数据
      addresses.value = originalAddresses
      ElMessage.error('删除失败')
    }
  })
}

// 新增地址（打开弹窗）
const handleAdd = () => {
  dialogTitle.value = '新增地址'
  isEdit.value = false
  editId.value = 0
  formData.receiver = ''
  formData.phone = ''
  formData.province = ''
  formData.city = ''
  formData.district = ''
  formData.detail = ''
  formData.isDefault = false
  dialogVisible.value = true
}

// 编辑地址（打开弹窗）
const handleEdit = (address: AddressItem) => {
  dialogTitle.value = '编辑地址'
  isEdit.value = true
  editId.value = address.id
  formData.receiver = address.receiver
  formData.phone = address.phone
  formData.province = address.province
  formData.city = address.city
  formData.district = address.district
  formData.detail = address.detail
  formData.isDefault = address.isDefault
  dialogVisible.value = true
}

// 设为默认地址（本地立即更新）
const handleSetDefault = async (addressId: number) => {
  // 保存原始数据（用于回滚）
  const originalAddresses = [...addresses.value]

  // 使用 map 返回新数组，确保触发响应式更新
  addresses.value = addresses.value.map((item) => ({
    ...item,
    isDefault: item.id === addressId
  }))
  ElMessage.success('已设为默认地址')

  try {
    await putAddressService(addressId)
    // 静默刷新，确保数据一致
    await getAddress()
  } catch (error) {
    console.error('设置默认失败:', error)
    // 失败时恢复数据
    addresses.value = originalAddresses
    ElMessage.error('设置失败')
  }
}

// 提交表单（新增/编辑）
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate()

  submitting.value = true

  const params = {
    receiver: formData.receiver,
    phone: formData.phone,
    province: formData.province,
    city: formData.city,
    district: formData.district,
    address: formData.detail,
    isDefault: formData.isDefault ? 1 : 0
  }

  console.log('提交的参数:', params)

  if (isEdit.value) {
    // 编辑地址（本地立即更新）
    const originalAddresses = [...addresses.value]
    const index = addresses.value.findIndex((item) => item.id === editId.value)

    if (index !== -1) {
      const existingAddress = addresses.value[index]
      if (existingAddress) {
        // 使用 splice 触发响应式更新
        addresses.value.splice(index, 1, {
          id: existingAddress.id,
          receiver: formData.receiver,
          phone: formData.phone,
          province: formData.province,
          city: formData.city,
          district: formData.district,
          detail: formData.detail,
          isDefault: formData.isDefault
        })
      }
    }

    dialogVisible.value = false
    ElMessage.success('修改成功')

    try {
      await deleteAddressService(editId.value)
      await addAddressService(params)
      await getAddress()
    } catch (error) {
      console.error('修改失败:', error)
      addresses.value = originalAddresses
      ElMessage.error('修改失败')
    }
  } else {
    // 新增地址（本地立即添加）
    const tempId = Date.now()
    const newAddress: AddressItem = {
      id: tempId,
      receiver: formData.receiver,
      phone: formData.phone,
      province: formData.province,
      city: formData.city,
      district: formData.district,
      detail: formData.detail,
      isDefault: formData.isDefault
    }

    addresses.value.push(newAddress)
    dialogVisible.value = false
    ElMessage.success('添加成功')

    try {
      await addAddressService(params)
      await getAddress()
    } catch (error) {
      console.error('添加失败:', error)
      // 失败时移除临时添加的数据
      addresses.value = addresses.value.filter((item) => item.id !== tempId)
      ElMessage.error('添加失败')
    }
  }

  submitting.value = false
}

onMounted(() => {
  getAddress()
})
</script>

<style scoped>
.address-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f8ff 0%, #e8f4ff 100%);
  padding: 40px 20px;
}

.page-header {
  max-width: 800px;
  margin: 0 auto 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-emoji {
  font-size: 32px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

.add-btn {
  border-radius: 40px;
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  border: none;
  padding: 10px 24px;
}

.btn-emoji {
  margin-right: 6px;
}

.loading-container {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 24px;
  padding: 60px 20px;
  text-align: center;
}

.anime-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.loading-emoji {
  font-size: 40px;
  animation: bounce 0.6s ease-in-out infinite;
}

.address-list {
  max-width: 800px;
  margin: 0 auto;
}

.address-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid #e8eef2;
  transition: all 0.3s;
}

.address-card.default {
  border-color: #6cb4ee;
  background: linear-gradient(135deg, #fff, #f0f8ff);
}

.address-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(108, 180, 238, 0.1);
}

.address-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.receiver {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.phone {
  font-size: 14px;
  color: #7f8c8d;
}

.default-badge {
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  color: white;
  padding: 2px 10px;
  border-radius: 40px;
  font-size: 12px;
}

.address-detail {
  font-size: 14px;
  color: #7f8c8d;
  line-height: 1.6;
}

.address-full {
  display: inline-block;
}

.address-actions {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #e8eef2;
  display: flex;
  gap: 20px;
}

.address-actions .el-button {
  color: #999;
  font-size: 13px;
}

.address-actions .el-button:hover {
  color: #6cb4ee;
}

.action-emoji {
  margin-right: 4px;
}

.empty-address {
  max-width: 500px;
  margin: 80px auto;
  background: white;
  border-radius: 32px;
  padding: 60px 40px;
  text-align: center;
}

.empty-anime {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-emoji {
  font-size: 64px;
  display: inline-block;
  margin: 0 8px;
  animation: shake 0.5s ease-in-out;
}

.empty-emoji:nth-child(2) {
  animation-delay: 0.1s;
}
.empty-emoji:nth-child(3) {
  animation-delay: 0.2s;
}

@keyframes shake {
  0%,
  100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(-15deg);
  }
  75% {
    transform: rotate(15deg);
  }
}

.empty-anime h3 {
  font-size: 24px;
  color: #2c3e50;
  margin: 16px 0 8px;
}

.empty-anime p {
  color: #7f8c8d;
  margin-bottom: 24px;
}

.add-empty-btn {
  border-radius: 40px;
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  border: none;
  padding: 12px 32px;
}

.address-dialog :deep(.el-dialog) {
  border-radius: 20px;
}

.address-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #e8eef2;
  padding: 20px 20px 15px;
}

.address-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.address-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.address-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid #e8eef2;
  padding: 15px 20px;
}

@media (max-width: 768px) {
  .address-page {
    padding: 20px 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .address-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .address-actions {
    flex-wrap: wrap;
  }
}
</style>
