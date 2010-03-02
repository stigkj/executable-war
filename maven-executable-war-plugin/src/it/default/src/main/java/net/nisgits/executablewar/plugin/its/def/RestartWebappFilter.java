package net.nisgits.executablewar.plugin.its.def;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.sun.akuma.Daemon;
import com.sun.akuma.JavaVMArguments;

import static net.nisgits.executablewar.plugin.its.def.GNUCLibrary.FD_CLOEXEC;
import static net.nisgits.executablewar.plugin.its.def.GNUCLibrary.F_GETFD;
import static net.nisgits.executablewar.plugin.its.def.GNUCLibrary.F_SETFD;
import static net.nisgits.executablewar.plugin.its.def.GNUCLibrary.LIBC;

public class RestartWebappFilter implements javax.servlet.Filter
{
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("############## STARTING UP - 1new");

	}

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

	public void destroy() {
		// Does nothing here yet
	}
}
