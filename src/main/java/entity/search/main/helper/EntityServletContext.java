/**
 * Linkipedia, Copyright (c) 2015 Tetherless World Constellation 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package entity.search.main.helper;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import entity.search.servlet.EntityAnnotationServlet;
import entity.search.servlet.EntityAnnotationServletNoFilter;
import entity.search.servlet.EntityLinkingServlet;
import entity.search.servlet.EntityLinkingWithValidationServlet;
import entity.search.servlet.EntityLinkingWithValidationServletSurface;
import entity.search.servlet.EntityLinkingWithValidationServletSurfaceSingle;
import entity.search.servlet.EntityLinkingWithValidationServletSurfaceSingleTop;
import entity.search.servlet.EntityRecognitionServlet;
import entity.search.servlet.EntitySearchServlet;
import entity.search.servlet.NLPServlet;
import entity.search.servlet.ReadIndexServlet;

public class EntityServletContext {

	
    public static void main(String[] args) throws Exception
    {
		if(args.length < 2){
			System.err.println("usage: Searcher index service_base port(optional)");
			return;
		}
		String index = args[0];	
		String service = args[1];		
		int port = 8080;		
		if(args.length == 3){
			port = Integer.parseInt(args[1]);
		}	
		
        Server server = new Server(port);	
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

		if(service.equals("nlp")){
	        context.addServlet(new ServletHolder(new NLPServlet()),"/nlp");

		}else{
			context.addServlet(new ServletHolder(new EntitySearchServlet(index)),"/search");
			context.addServlet(new ServletHolder(new EntityLinkingServlet(index)),"/linking");
		}             
        //context.addServlet(new ServletHolder(new EntitySurfaceFormServlet()),"/surface");
 
        server.start();
        server.join();
    }
    
    public static void startServlet(String index, String surfaceFormIndex, int port){
		try{
			//problem here
	        Server server = new Server(port);
	 
	        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        context.setContextPath("/");
	        server.setHandler(context);

			context.addServlet(new ServletHolder(new EntitySearchServlet(index)),"/search");
//			context.addServlet(new ServletHolder(new EntityLinkingServlet(index)),"/linking");
			context.addServlet(new ServletHolder(new ReadIndexServlet(index)),"/read");
			//kosova -> kosovo
//			context.addServlet(new ServletHolder(new EntityLinkingWithValidationServlet(index)),"/vlinking");
//			context.addServlet(new ServletHolder(new EntityLinkingWithValidationServletSurface(index,surfaceFormIndex)),"/vlinking2");
			//context.addServlet(new ServletHolder(new EntityRecognitionServlet(surfaceFormIndex)),"/recognition");
			context.addServlet(new ServletHolder(new EntityLinkingWithValidationServletSurfaceSingle(index,surfaceFormIndex)),"/vlinking3");
			context.addServlet(new ServletHolder(new EntityLinkingWithValidationServletSurfaceSingleTop(index,surfaceFormIndex)),"/vlinkingtop");
			//context.addServlet(new ServletHolder(new EntityAnnotationServlet(index,surfaceFormIndex)),"/annotating");
			//context.addServlet(new ServletHolder(new EntityAnnotationServletNoFilter(index,surfaceFormIndex)),"/annotating2");
			//context.addServlet(new ServletHolder(new NLPServlet()),"/nlp");
			    
	        server.start();
	        server.join();
		}catch(Exception e){
			e.printStackTrace();
		}
    }
	
}
