import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Form, Input, InputNumber, Select, Button, Upload, message } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { createProduct } from '../api/product'
import type { CreateProductParams } from '../api/product'
import { uploadImage } from '../api/upload'
import type { UploadFile, UploadProps } from 'antd'

const { TextArea } = Input
const { Option } = Select

const categories = ['电子产品', '书籍教材', '生活用品', '服饰鞋包', '运动户外', '其他']

const PublishProduct = () => {
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [fileList, setFileList] = useState<UploadFile[]>([])
  const [imageUrl, setImageUrl] = useState('')

  const handleUpload: UploadProps['customRequest'] = async (options) => {
    const { file, onSuccess, onError } = options
    try {
      const result = await uploadImage(file as File)
      setImageUrl(result.imageUrl)
      onSuccess?.(result)
      message.success('图片上传成功')
    } catch (error) {
      onError?.(error as Error)
    }
  }

  const handleChange: UploadProps['onChange'] = ({ fileList: newFileList }) => {
    setFileList(newFileList)
  }

  const onFinish = async (values: Omit<CreateProductParams, 'imageUrl'>) => {
    if (!imageUrl) {
      message.warning('请上传商品图片')
      return
    }
    
    setLoading(true)
    try {
      await createProduct({ ...values, imageUrl })
      message.success('发布成功')
      navigate('/my-products')
    } catch {
      // 错误已在拦截器中处理
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card title="发布商品" style={{ maxWidth: 600, margin: '0 auto' }}>
      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
      >
        <Form.Item
          label="商品图片"
          required
        >
          <Upload
            listType="picture-card"
            fileList={fileList}
            customRequest={handleUpload}
            onChange={handleChange}
            maxCount={1}
            accept="image/jpeg,image/png"
          >
            {fileList.length < 1 && (
              <div>
                <PlusOutlined />
                <div style={{ marginTop: 8 }}>上传图片</div>
              </div>
            )}
          </Upload>
          <div style={{ color: '#999', fontSize: 12 }}>支持jpg/png格式，最大2MB</div>
        </Form.Item>

        <Form.Item
          name="name"
          label="商品名称"
          rules={[
            { required: true, message: '请输入商品名称' },
            { max: 50, message: '商品名称最多50个字符' }
          ]}
        >
          <Input placeholder="请输入商品名称" />
        </Form.Item>

        <Form.Item
          name="description"
          label="商品描述"
          rules={[
            { required: true, message: '请输入商品描述' },
            { max: 500, message: '商品描述最多500个字符' }
          ]}
        >
          <TextArea rows={4} placeholder="请输入商品描述" />
        </Form.Item>

        <Form.Item
          name="price"
          label="价格"
          rules={[
            { required: true, message: '请输入价格' },
            { type: 'number', min: 0.01, message: '价格必须大于0' }
          ]}
        >
          <InputNumber
            style={{ width: '100%' }}
            prefix="¥"
            precision={2}
            placeholder="请输入价格"
          />
        </Form.Item>

        <Form.Item
          name="category"
          label="分类"
          rules={[{ required: true, message: '请选择分类' }]}
        >
          <Select placeholder="请选择分类">
            {categories.map(cat => (
              <Option key={cat} value={cat}>{cat}</Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block>
            发布商品
          </Button>
        </Form.Item>
      </Form>
    </Card>
  )
}

export default PublishProduct
