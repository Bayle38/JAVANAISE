package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public class JvnInterceptor implements InvocationHandler{
	JvnObject o;
	public JvnInterceptor(JvnObject obj){
		o = obj;
	}
	public static Object register(String name, Serializable obj) throws JvnException{
		JvnObject jo = JvnServerImpl.jvnGetServer().jvnCreateObject(obj);
		JvnServerImpl.jvnGetServer().jvnRegisterObject(name, jo);
		return java.lang.reflect.Proxy.newProxyInstance(
				obj.getClass().getClassLoader(),
				obj.getClass().getInterfaces(), 
				new JvnInterceptor(jo)
		);
	}
	public static Object lookup(String name) throws JvnException{
		JvnObject jo = JvnServerImpl.jvnGetServer().jvnLookupObject(name);
		if(jo!=null){
			return java.lang.reflect.Proxy.newProxyInstance(
					jo.jvnGetObjectState().getClass().getClassLoader(),
					jo.jvnGetObjectState().getClass().getInterfaces(), 
					new JvnInterceptor(jo)
			);
		}
		else return null;
	}
	
	@Override
	public Object invoke(Object ji, Method m, Object[] args)
			throws Throwable {
			Object result = null;
			try{
				System.out.println("avant");
				result = m.invoke(o.jvnGetObjectState(), args);
				System.out.println("après");
			}catch(Exception e){
				e.printStackTrace();
			}
		return result;
	}

}
