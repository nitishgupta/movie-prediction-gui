package project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.matrix.Matrix;
import weka.core.matrix.SingularValueDecomposition;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class MoviePrediction {

	public static Matrix v;
	public static Matrix s;
	public static Matrix u;
	public static double[][] pseudo;
	
	public static void run(double[] error, String file, boolean complete) throws Exception
	{
		if(complete)
		{
			train(file,false);
			varyK(11);
			return;
		}
		train(file,false);
		error[0]=getPseudoError(file);
		error[1]=test(file);
	}

	private static double test(String test) throws Exception 
	{
		String line;
		double[][] predictions = new double[1682][943];
		double error=0.0;
		int k=11;
		
		BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir")+"\\data\\"+test+".test"));
		predictions = varyK(k);
		while((line=reader.readLine())!=null)
		{
			String[] parts=line.split("\\s+");
			if(parts.length>=3)
			{
				int userId = Integer.parseInt(parts[0]);
				int movieId = Integer.parseInt(parts[1]);
				System.out.println("User id= "+userId+" MovieId= "+movieId);
				double actual = Integer.parseInt(parts[2]);
				error+=(Math.abs(actual-predictions[movieId-1][userId-1]))/5.0;
			}
		}
		reader.close();
		return error;
	}
	
	private static double getPseudoError(String test) throws Exception
	{
		double error=0.0;
		String line="";
		BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir")+"\\data\\"+test+".test"));
		
		while((line=reader.readLine())!=null)
		{
			String[] parts=line.split("\\s+");
			if(parts.length>=3)
			{
				int userId = Integer.parseInt(parts[0]);
				int movieId = Integer.parseInt(parts[1]);
				System.out.println("User id= "+userId+" MovieId= "+movieId);
				double actual = Integer.parseInt(parts[2]);
				error+=(Math.abs(actual-pseudo[userId-1][movieId-1]))/5.0;
			}
		}
		reader.close();
		return error;
	}

	private static double[][] varyK(int k) throws Exception 
	{
		double[][] predictions;
		s=s.getMatrix(0, k, 0, k);
		Matrix sQ = s.sqrt();
		Matrix us = u.getMatrix(0, u.getRowDimension()-1, 0, k).times(sQ.transpose());
		Matrix sv = sQ.times(v.getMatrix(0, v.getRowDimension()-1, 0, k).transpose());
		Matrix p= us.times(sv);
		predictions = p.getArray();
		writeMatrixToCsv(predictions, u.getRowDimension(), v.getRowDimension(), "output");
		return predictions;
	}

	private static void train(String train,boolean knnTest) throws Exception 
	{
		DataSource reader = new DataSource(System.getProperty("user.dir")+"\\data\\"+train+".csv");
		Instances ratings = reader.getDataSet();
		System.out.println(ratings.numInstances());
		ratings.sort(ratings.attribute(ratings.attribute(0).name()));
		
		if(knnTest==true)
			reader=new DataSource(System.getProperty("user.dir")+"\\data\\usersKnn.csv");
		else
			reader = new DataSource(System.getProperty("user.dir")+"\\data\\user.csv");
		Instances users = reader.getDataSet();
		System.out.println(users.numInstances());
		
		reader = new DataSource(System.getProperty("user.dir")+"\\data\\item.csv");
		Instances movies = reader.getDataSet();
		System.out.println(movies.numInstances());
		movies.sort(movies.attribute(0));
		
		double[][] ratingsMatrix= new double[users.numInstances()][movies.numInstances()];
		int j=0;
		
		double[][] viewedMatrix= new double[users.numInstances()][movies.numInstances()];
		
		ArrayList<Integer> userMoviesId = new ArrayList<Integer>();
		
		int currentUser =1;
		int lastUser = 1;
		int size=0;
		int start =0;
		int movieId=0;
		
		while(j<ratings.numInstances())
		{
			currentUser = (int) ratings.instance(j).value(0);
			movieId = (int) ratings.instance(j).value(1);
			
			if(currentUser == lastUser)
			{
				size++;
				ratingsMatrix[currentUser-1][movieId-1] = (double)ratings.instance(j).value(2);
				viewedMatrix[currentUser-1][movieId-1]=1;
				userMoviesId.add(movieId);
				j++;
			}
			else
			{
				Instances temp =  new Instances(ratings,start,size);
				createMoviesForUser(lastUser, temp, movies);
				fillPseudoRanking(lastUser,userMoviesId,ratingsMatrix,movies);
				size=0;
				userMoviesId.clear();
				start =j;
			}
			lastUser = currentUser;
		}
		Instances temp = new Instances(ratings,start,size);
		createMoviesForUser(lastUser, temp, movies);
		fillPseudoRanking(lastUser,userMoviesId,ratingsMatrix,movies);
		
		writeMatrixToCsv(viewedMatrix, users.numInstances(), movies.numInstances(), "view");
		writeMatrixToCsv(ratingsMatrix,users.numInstances(),movies.numInstances(),"pseudo");
		pseudo =ratingsMatrix.clone();
		generatePredictions(ratingsMatrix,users.numInstances(),movies.numInstances());
	}

	private static void generatePredictions(double[][] ratingsMatrix,
			int numUserInstances, int numMovieInstances) 
	{
		Matrix rm = new Matrix(ratingsMatrix);
		rm=rm.transpose();
		System.out.println("Starting svd for matrix of size " + rm.getRowDimension()+" " +rm.getColumnDimension());
		SingularValueDecomposition svd = rm.svd();
		System.out.println("SVD done" +svd.toString());
		u = svd.getU();
		s = svd.getS();
		v = svd.getV();
	}

	private static void writeMatrixToCsv(double[][] ratingsMatrix,
			int numUserInstances, int numMovieInstances, String name) throws Exception 
	{
		BufferedWriter w = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+"\\data\\"+name+".csv"));
		for(int a=0;a<numUserInstances;a++)
		{
			for(int b=0;b<numMovieInstances;b++)
			{
				if(b==numMovieInstances-1)
					w.write(ratingsMatrix[a][b]+"\n");
				else
					w.write(ratingsMatrix[a][b]+",");
			}
		}
		w.close();
	}

	private static void fillPseudoRanking(int userId,
			ArrayList<Integer> userMoviesId, double[][] ratingsMatrix,
			Instances movies) throws Exception 
	{
		DataSource reader = new DataSource(System.getProperty("user.dir")+"\\data\\ex"+userId+".arff");
		Instances rs = reader.getDataSet();
		rs.setClassIndex(rs.numAttributes()-1);
		
		//removing the first attribute id
		Remove remove = new Remove();
		remove.setAttributeIndices("1");
		remove.setInputFormat(rs);
		Instances data =Filter.useFilter(rs, remove); 
		
		//creating a Naive Bayes based classifier
		NaiveBayes nb = new NaiveBayes();
		nb.buildClassifier(data);
		
		//begin classifying and getting pseudo ratings
		for(int i=0;i<movies.numInstances();i++)
		{
			int m = (int) movies.instance(i).value(0);
			if(userMoviesId.contains(m))
				continue;
			
			Instance in = new Instance(data.numAttributes());
			in.setDataset(data);
			
			//creating the instance to be classified
			for(int j=1;j<data.numAttributes();j++)
				in.setValue(j-1, movies.instance(i).value(j));
			
			//setting the rating
			ratingsMatrix[userId-1][(int) movies.instance(i).value(0)-1] = nb.classifyInstance(in);
		}
	}

	private static void createMoviesForUser(int userId, Instances ratings,
			Instances movies) throws Exception 
	{
		ratings.deleteAttributeAt(0);
		ratings.sort(ratings.attribute(0));
		
		//uMovies := instances of a movis of a particular users
		Instances uMovies = new Instances(movies,(int)ratings.instance(0).value(0)-1,1);
		
		//adding the necssary movies in uMovies
		int counter =0;
		for(int k=1;k<ratings.numInstances();k++)
		{
			while(ratings.instance(k).value(0)!=movies.instance(counter).value(0))
				counter++;
			uMovies.add(movies.instance(counter));
		}
		
		//adding an attribute of rating in uMovies
		uMovies.insertAttributeAt(ratings.attribute(1), uMovies.numAttributes());
		
		//initializing the attribute by respective ratings
		for(int k=0;k<ratings.numInstances();k++)
		{
			int d=(int) ratings.instance(k).value(1);
			uMovies.instance(k).setValue(uMovies.attribute(uMovies.numAttributes()-1),d);
		}
		
		//create the arff File of the particular user
		makeArff(uMovies,userId);
	}

	private static void makeArff(Instances uMovies, int userId) throws Exception 
	{
		BufferedWriter w = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+"/data/ex"+userId+".arff"));
		String s="@relation item\n"+
				"@attribute Id numeric\n"+
				"@attribute Other {0,1}\n"+
				"@attribute Action {0,1}\n"+
				"@attribute Adventure {0,1}\n"+
				"@attribute Animation {0,1}\n"+
				"@attribute Children {0,1}\n"+
				"@attribute Comedy {0,1}\n"+
				"@attribute Crime {0,1}\n"+
				"@attribute Documentary {0,1}\n"+
				"@attribute Drama {0,1}\n"+
				"@attribute Fantasy {0,1}\n"+
				"@attribute Film-Nor {0,1}\n"+
				"@attribute Horror {0,1}\n"+
				"@attribute Musical {0,1}\n"+
				"@attribute Mystery {0,1}\n"+
				"@attribute Romance {0,1}\n"+
				"@attribute Sci-Fi {0,1}\n"+
				"@attribute Thriller {0,1}\n"+
				"@attribute War {0,1}\n"+
				"@attribute Western {0,1}\n" +
				"@attribute class {1,2,3,4,5}\n\n"+
				"@data";
		w.write(s);
		w.newLine();
		for(int i=0;i<uMovies.numInstances();i++)
		{
			w.write(uMovies.instance(i).toString());
			w.newLine();
		}
		w.close();
	}

	@SuppressWarnings("unused")
	private static void testKNN() throws Exception
	{
		train("rating1",true);
		double[][] predictions = varyK(11);
		for(int k=1;k<20;k++)
		{
			BufferedReader reader=new BufferedReader(new FileReader(System.getProperty("user.dir")+"/data/rating1.test.csv"));
			String line=reader.readLine();
			double error=0.0;
			while((line=reader.readLine())!=null)
			{
				String parts[] = line.split(",");
				int userId=Integer.parseInt(parts[0]);
				int movieId=Integer.parseInt(parts[1]);
				double actual = Double.parseDouble(parts[2]);
				String[] demographics = getDemographics(userId);//System.out.println("User id= "+userId+" Demographics="+Arrays.toString(demographics));
				double[] neighbours=new double[k];
				if(demographics==null) continue;
				neighbours= getNearestNeighbours(Double.parseDouble(demographics[1]), Double.parseDouble(demographics[4]), demographics[3], demographics[2], k);
				double avg=0.0;
				for(int a=0;a<k;a++)
				{
					avg+=predictions[movieId][(int) (neighbours[a]-93)];
				}
				avg=avg/k;
				error+=Math.abs(actual-avg)/5.0;
			}
			System.out.println("Error for k= "+k +"= "+ error);
			reader.close();
		}
	}
	
	@SuppressWarnings("resource")
	private static String[] getDemographics(int userId) throws Exception {
		// TODO Auto-generated method stub
		BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/data/user1knn.test.csv"));
		String line=reader.readLine();
		while((line=reader.readLine())!=null)
		{
			String[] parts = line.split(",");
			if(userId == Integer.parseInt(parts[0]))
			{
				return parts;
			}
		}
		reader.close();
		return null;
	}

	public static double[] getNearestNeighbours(double age, double zip, String prof, String sex, int k) throws Exception
	{
		DataSource reader = new DataSource("C:/Users/divyanshu/Documents/AI/MoviePrediction/data/user1knn.csv");
		Instances users = reader.getDataSet();
		double[] nearestNeighbours=new double[k];
		
		double ageMin = users.attributeStats(1).numericStats.min;
		double ageMax = users.attributeStats(1).numericStats.max;
		double ageRange = ageMax - ageMin;
		double zipMin = users.attributeStats(4).numericStats.min;
		double zipMax = users.attributeStats(4).numericStats.max;
		double zipRange = zipMax - zipMin;
		double [] distance=new double[users.numInstances()];
		int[] userIds=new int[users.numInstances()];
		double wz=1,wa=1,ws=1,wp=1;
		
		//System.out.println(age+" "+sex+" "+prof+" "+zip);
		String s="",p="";
		for(int i=0;i<users.numInstances();i++)
		{
			userIds[i]=(int) users.instance(i).value(0);
		}
		for(int i=0; i<users.numInstances(); i++)
		{
			double distanceAge=0, distanceSex=0, distanceProf=0, distanceZip=0;
			
			distance[i] = 0;
			switch((int)users.instance(i).value(2))
			{
			case 0: s="M";break;
			case 1: s="F";break;
			}
			
			switch((int)users.instance(i).value(3))
			{
			case 0: p="administrator";break;
			case 1: p="artist";break;
			case 2: p="doctor";break;
			case 3: p="educator";break;
			case 4: p="engineer";break;
			case 5: p="entertainment";break;
			case 6: p="executive";break;
			case 7: p="healthcare";break;
			case 8: p="homemaker";break;
			case 9: p="lawyer";break;
			case 10: p="librarian";break;
			case 11: p="marketing";break;
			case 12: p="other";break;
			case 13: p="none";break;
			case 14: p="programmer";break;
			case 15: p="retired";break; 
			case 16: p="salesman";break;
			case 17: p="scientist";break;
			case 18: p="student";break;
			case 19: p="technician";break;
			case 20: p="writer";break;
			}
			
			if( !s.equalsIgnoreCase(sex))
				distanceSex = 1;
			if( !p.equalsIgnoreCase(prof))
				distanceProf = 1;
			
			distanceAge = Math.pow(( (users.instance(i).value(1) - ageMin)/ageRange -  (age - ageMin)/ageRange ), 2);
			distanceZip = Math.pow(( (users.instance(i).value(4) - zipMin)/zipRange -  (zip - zipMin)/zipRange ), 2);
			
			distance[i] = wa*distanceAge + ws*distanceSex + wp*distanceProf + wz*distanceZip;
		}
		for(int i=0;i<users.numInstances()-1;i++)
		{
			for(int j=0;j<users.numInstances()-i-1;j++)
			{
				if(distance[j]>distance[j+1])
				{
					double tmp=distance[j];
					distance[j]=distance[j+1];
					distance[j+1]=tmp;
					int tm=userIds[j];
					userIds[j]=userIds[j+1];
					userIds[j+1]=tm;
				}
			}
		}
		//System.out.println(distance[0]+" "+distance[1]+" "+distance[2]+" "+distance[3]+" "+distance[4]+" "+distance[5]+" ");
		//System.out.println(userIds[0]+" "+userIds[1]+" "+userIds[2]+" "+userIds[3]+" "+userIds[4]+" "+userIds[5]+" ");
		for(int a=0;a<k;a++)
		{
			nearestNeighbours[a]=userIds[a];
		}
		return nearestNeighbours;
	}
	
	public static void main(String arg[])throws Exception
	{
		run(null,"rating",true);
		//double[] error=new double[2];
		//run(error, "u1");
		//System.out.println("Pseudo error: "+error[0]+" Final error: "+error[1]);
		//testKNN();
	}
}
