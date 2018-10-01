package cn.gp.util;

import cn.gp.model.Request;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * 工具类
 */
public class ByteAndObject {

	private static Schema<Request> schema = RuntimeSchema.createFrom(Request.class);

	/**
	 * obj转换为bytes
	 * @param request 对象
	 * @return 字节数组
	 */
	public static byte[] serialize(Request request) {

		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try {
			return ProtostuffIOUtil.toByteArray(request,schema,buffer);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			buffer.clear();
		}
	}

	/**
	 * bytes转为指定对象(暂定只转化一种对象)
	 * @param data 数组
	 * @return Request对象
	 */
	public static Request deserialize(byte[] data) {
		try {
			Request message = schema.newMessage();
			ProtostuffIOUtil.mergeFrom(data,message,schema);
			return message;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
