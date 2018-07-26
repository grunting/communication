package cn.gp.model;

/**
 * 测试模型
 */
public class Request {

    // 访问的id(方便回传时获取返回值)
    private Integer id;

    // 服务名
    private String serviceName;

    // 函数名
    private String methodName;

    // 参数类型列表
    private Class<?>[] parameterTypes;

    // 参数列表
    private Object[] arguments;

    // 返回值
    private Object result;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
