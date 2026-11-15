<template>
  <div>
    <div style="margin-bottom: 20px">
      <el-button type="success" @click="handleExport">导出</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <div style="margin-bottom: 20px">
      <el-input v-model="data.username" placeholder="请输入用户名" style="width: 200px; margin-right: 10px"></el-input>
      <el-select v-model="data.type" placeholder="请选择反馈类型" style="width: 200px; margin-right: 10px">
        <el-option label="功能建议" value="suggestion"></el-option>
        <el-option label="问题反馈" value="bug"></el-option>
        <el-option label="其他" value="other"></el-option>
      </el-select>
      <el-select v-model="data.status" placeholder="请选择状态" style="width: 200px; margin-right: 10px">
        <el-option label="待处理" value="待处理"></el-option>
        <el-option label="已处理" value="已处理"></el-option>
        <el-option label="已忽略" value="已忽略"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <el-table :data="data.tableData" stripe border>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="username" label="用户名" width="120"></el-table-column>
      <el-table-column prop="type" label="反馈类型" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 'suggestion'" type="success">功能建议</el-tag>
          <el-tag v-else-if="scope.row.type === 'bug'" type="danger">问题反馈</el-tag>
          <el-tag v-else type="info">其他</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="反馈内容" show-overflow-tooltip></el-table-column>
      <el-table-column prop="contact" label="联系方式" width="150"></el-table-column>
      <el-table-column prop="time" label="提交时间" width="180"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '待处理'" type="warning">待处理</el-tag>
          <el-tag v-else-if="scope.row.status === '已处理'" type="success">已处理</el-tag>
          <el-tag v-else type="info">已忽略</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button 
            v-if="scope.row.status === '待处理'" 
            type="success" 
            @click="handleProcess(scope.row)" 
            size="small">处理</el-button>
          <el-button type="danger" @click="handleDelete(scope.row.id)" size="small">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px">
      <el-pagination
          background
          @current-change="handleCurrentChange"
          :current-page="data.pageNum"
          :page-size="data.pageSize"
          layout="prev, pager, next"
          :total="data.total">
      </el-pagination>
    </div>

    <el-dialog title="处理意见反馈" width="40%" v-model="data.processVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.processForm" label-width="100px" style="padding-right: 50px">
        <el-form-item label="用户名">
          <el-input v-model="data.processForm.username" disabled />
        </el-form-item>
        <el-form-item label="反馈类型">
          <el-input v-model="data.processForm.typeText" disabled />
        </el-form-item>
        <el-form-item label="反馈内容">
          <el-input type="textarea" v-model="data.processForm.content" :rows="4" disabled />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="data.processForm.contact" disabled />
        </el-form-item>
        <el-form-item label="提交时间">
          <el-input v-model="data.processForm.time" disabled />
        </el-form-item>
        <el-form-item label="处理状态" prop="status">
          <el-select v-model="data.processForm.status" placeholder="请选择处理状态">
            <el-option label="已处理" value="已处理"></el-option>
            <el-option label="已忽略" value="已忽略"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.processVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveProcess">确认处理</el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  processVisible: false,
  processForm: {},
  tableData: [],
  username: null,
  type: null,
  status: null
})

// 分页查询
const load = () => {
  request.get('/feedback/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      username: data.username,
      type: data.type,
      status: data.status
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}

// 处理反馈
const handleProcess = (row) => {
  data.processForm = JSON.parse(JSON.stringify(row))
  // 设置反馈类型的显示文本
  if (data.processForm.type === 'suggestion') {
    data.processForm.typeText = '功能建议'
  } else if (data.processForm.type === 'bug') {
    data.processForm.typeText = '问题反馈'
  } else {
    data.processForm.typeText = '其他'
  }
  data.processVisible = true
}

// 保存处理结果
const saveProcess = () => {
  if (!data.processForm.status) {
    ElMessage.warning('请选择处理状态')
    return
  }
  
  request.put('/feedback/update', data.processForm).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('处理成功')
      data.processVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/feedback/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 重置
const reset = () => {
  data.username = null
  data.type = null
  data.status = null
  load()
}

// 分页
const handleCurrentChange = (pageNum) => {
  data.pageNum = pageNum
  load()
}

load()
</script>
