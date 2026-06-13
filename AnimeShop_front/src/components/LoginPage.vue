<template>
  <div id="loginpage">
    <el-form
      ref="formRefLogin"
      :model="formLogin"
      :rules="rules"
      label-width="80px"
      size="large"
      label-position="top"
      @submit.prevent="onSubmit"
    >
      <h2>登录</h2>
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="formLogin.username"
          placeholder="请输入用户名"
          prefix-icon="User"
        ></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="formLogin.password"
          type="password"
          placeholder="请输入密码"
          show-password
          prefix-icon="Lock"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" native-type="submit">登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { type FormRules, type FormInstance, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import { useRouter } from 'vue-router'

// 登录表单绑定的数据
const formLogin = reactive({
  username: '',
  password: ''
})

// 添加表单引用，类型是 FormInstance
const formRefLogin = ref<FormInstance>() //错误不影响，因为在模板中已经正确绑定了 ref="formRef"

const userStore = useUserStore()

const router = useRouter()
// 登录按钮点击事件，这里由于登录按钮在el-form标签中，因此el-from会自动触发一次表单验证
const onSubmit = async () => {
  if (!formRefLogin.value) return //表单提交时候没有值就不执行提交
  try {
    //数据获取, validate()方法会自动校验表单数据
    await formRefLogin.value.validate()
    //调用pinia中的保存用户信息的方法,登录成功后才往下执行
    await userStore.getUserInfo(formLogin.username, formLogin.password)
    //登录成功提示
    ElMessage.success('登录成功！') //验证成功提示
    // 暂时跳转到用户页面，后续可以根据用户角色进行不同的跳转
    router.push('/user')
  } catch (error) {
    ElMessage.error('账号或密码错误') //验证失败提示
  }
}

//表单验证规则
const rules = reactive<FormRules>({
  username: [
    {
      required: true, //必填项验证
      message: '请输入用户名', //验证失败提示
      trigger: 'blur'
    }, //失去焦点时触发验证
    {
      min: 4, //最小长度验证
      max: 10, //最大长度验证
      message: '长度在 4 到 10 个字符',
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 15, message: '长度在 6 到 15 个字符', trigger: 'blur' }
  ]
})
</script>

<style scoped>
/* 基础布局 */
#loginpage {
  display: flex;
  background-color: #f5f5f5;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100%;
}

/* 表单整体样式 */
#loginpage .el-form {
  background-color: #fff;
  flex-direction: column;
  padding: 20px;
  width: 400px;
}

/* 标题样式 */
#loginpage .el-form h2 {
  font-size: 25px;
  margin-bottom: 15px;
}

/* 按钮样式 */
#loginpage .el-form .el-form-item .el-button {
  margin-top: 10px;
  width: 100%;
  border-radius: 10px;
}
</style>
