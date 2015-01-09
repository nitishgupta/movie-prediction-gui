package project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class MovieFormHandle
 */
@WebServlet("/MovieFormHandle")
public class MovieFormHandle extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static int movieId;
    static HashMap<Integer,String> movieMap=null;  
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MovieFormHandle() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		movieId=0;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("C:/Users/divyanshu/Documents/AI/MoviePrediction/data/movie.csv"));
			String line=reader.readLine();
			movieMap = new HashMap<Integer,String>();
			
			while((line=reader.readLine())!=null)
			{
				//System.out.println(line);
				String values="";
				String parts[] = line.split(",");
				int key=Integer.parseInt(parts[0]);
				values=parts[1];
				movieMap.put(new Integer(key), values);
			}
			reader.close();
			System.out.println("User hash map create");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("In movie servlet");
		String id=request.getParameter("movie");
		System.out.println(id);
		movieId=(int) Double.parseDouble(id);
		PrintWriter out=response.getWriter();
		out.write(movieMap.get(movieId));
	}

}
