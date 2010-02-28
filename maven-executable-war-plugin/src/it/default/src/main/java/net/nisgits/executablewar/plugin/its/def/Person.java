package net.nisgits.executablewar.plugin.its.def;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

import com.sun.akuma.Daemon;
import com.sun.akuma.JavaVMArguments;

import static net.nisgits.executablewar.plugin.its.def.GNUCLibrary.*;

public class Person implements javax.servlet.Filter
{
    private String name;
    
    public void setName( String name )
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("############## STARTING UP - 1new");

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		System.out.println("############## RESTART PROCESS - 1new");
/*
		final Daemon daemon = new Daemon();
		try {
			System.clearProperty(Daemon.class.getName());
			daemon.all(true);
		} catch (Exception e) {
			throw new ServletException("Could not daemonize", e);
		}
*/
		final JavaVMArguments javaVMArguments = JavaVMArguments.current();

		int sz = LIBC.getdtablesize();
		System.out.println("size = " + sz);
        for(int i=3; i<sz; i++) {
            int flags = LIBC.fcntl(i, F_GETFD);
            if(flags<0) continue;
            LIBC.fcntl(i, F_SETFD,flags| FD_CLOEXEC);
        }

		System.out.println("############## RESTART PROCESS - 2");
		Daemon.selfExec(javaVMArguments);
		System.out.println("############## RESTART PROCESS - 3");
		throw new IllegalStateException("Could not restart process");
	}

	@Override
	public void destroy() {
		// FIXME implement method
	}
}
