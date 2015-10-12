package jvn;


import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public class JvnInterceptor implements InvocationHandler{
	JvnObject o;
	Class c;
	public JvnInterceptor(String name, Class c) throws JvnException{
		JvnObject jo = JvnServerImpl.jvnGetServer().jvnLookupObject(name);
		if(jo == null){
			try {
				jo = JvnServerImpl.jvnGetServer().jvnCreateObject((Serializable) c.newInstance());
				JvnServerImpl.jvnGetServer().jvnRegisterObject(name, jo);
				this.c = c;
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				throw new JvnException("Erreur de class");
			}
		}
		o = jo;
	}
	public static Object newInstance(String name, Class c) throws JvnException{
		return java.lang.reflect.Proxy.newProxyInstance(
				c.getClassLoader(),
				c.getInterfaces(), 
				new JvnInterceptor(name,c)
		);
	}
//	public static Object lookup(String name) throws JvnException{
//		if(jo!=null){
//			return java.lang.reflect.Proxy.newProxyInstance(
//					jo.jvnGetObjectState().getClass().getClassLoader(),
//					jo.jvnGetObjectState().getClass().getInterfaces(), 
//					new JvnInterceptor(jo)
//			);
//		}
//		else return null;
//	}
	
	@Override
	public Object invoke(Object ji, Method m, Object[] args)
			throws Throwable {
			Object result = null;
			try{
				Annotation an = m.getAnnotation(Operation.class);
				if (((Operation) an).type() == "read"){
					o.jvnLockRead();
					System.out.println("Read");
				}else if(((Operation) an).type() == "write"){
					o.jvnLockWrite();
					System.out.println("Write");
				}
				result = m.invoke(o.jvnGetObjectState(), args);
				o.jvnUnLock();
				System.out.println("Unlock");
			}catch(Exception e){
				e.printStackTrace();
			}
		return result;
	}

}
