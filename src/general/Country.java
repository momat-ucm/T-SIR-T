package general;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Country {
	
	String name;
	double totalpop;
	int numseries; // number of available time series about the disease
	int numerrors; // number of errors to be computed
	DateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
	static String currentFolder = System.getProperty("user.dir");
	public static final String SEPARATOR = ";"; // separator csv
	String path;
	String outputpath;
	
	// 1) Disease characteristics
	int numstates = 7; // number of states involved in the model system (that cannot be decoupled)
	//states[t] = {S[t],E[t],I[t],Iu[t],HR[t], HD[t], Q[t]};
	String dfile     = "disease.csv";
	public double[] csvdur = new double[numstates-1];
	public double   csvbetai ;
	public double[] csvcoef   = new double[2];
	public String[] csvdatesch   = new String   [numstates-1];  // for sudden changes in durations
	public String[] csvdateschlin= new String[2*(numstates-1)]; // for linear changes in durations
	public boolean[] dursuddench = new boolean[numstates-1];    // for sudden changes in durations
	public boolean[] durlinch    = new boolean[numstates-1];    // for linear changes in durations
	public int[] tstepdurch      = new int    [numstates-1];    // for sudden changes in durations
	public int[] tstepdurchlin   = new int [2*(numstates-1)];   // for linear changes in durations
	public double[] csvFR        = new double[3];
	public double csvp ;
	
	// 2) Control measures
	int ncm = 50; // Max. number of different control measures or different levels of control measures
	String[] datecm   = new String[ncm]; 
	int[] lambda      = new int[ncm];
	String cmfile     = "controlmeasures.csv";
	public double[] csvm     = new double[ncm+1];
	public double[] csvkappa = new double[ncm+1];
	
	// 3) Reported data
	String[] dates;
	int firstIndex=0;
	int Nhist;  // Number of historical data
	int NhistC; // Number of considered historical data
	double[][] repdata; 
	// repdata is formed by: 
	// repdata[t][0] = datosic[t]; repdata[t][1] = datosid[t]; repdata[t][2] = datoH[t];    repdata[t][3] = datoQ[t];
	// repdata[t][4] = datoR[t];   repdata[t][5] = datoHW[t];  repdata[t][6] = datoImpE[t]; repdata[t][7] = datoEvac[t]; 
	// repdata[t][8] = datoUnD[t]; repdata[t][9] = datoV[t];
	String tsfile = "timeseries.csv";
	
	// 4) Errors
	// Number of the time step that should be compared
	int[] tstepc;
	double[] timec;
	double[] normRepData; 
	// normRepData is formed by: {normCC, normCD, normH, normQ, ...}
	double[] rho = {10.0, 100.0}; // to penalize 
	
	// 5) SIMULATION DATA
	String datei;
	String datef; 
	int dmax; // dmax: Number of days between datei and datef
	double dt; 
	Date dateinit;
	Date datefin;
	int[] timestheta;
	
	// 6) UNDETECTED DEATHS
	double[] repUnD;
	int[] tund;
	double[] timecund;
	double normRepDataUnD; 
	
	// 7) VARIANTS 
	int nvariants = 1; //at least there is 1 variant of the virus, the reference variant
	double[] csvk;
	double[] csvfr;
	
	// 8) VACCINES
	int nvaccines = 0;
	int[] initvacc;
	String[] dateinitvacc;
	double[][] efficacy;
	int[][] daysefficacy;
	int[][] nvacweek;
	int nmaxeff = 9; // maximum number of different efficacies
	int nmaxweeks=9; // maximum number of different rates doses/week
	int[] days2nd;
	int tRdvacdays = 0;
	
	// 9) EVOL COEF MORT DUE TO VACCINATION and EVOL PROPORTION OF VARIANTS
	double[] coefmort;
	double[] coefbetaH;
	double[][] propvar;
	String coefmortfile = "variant_and_vaccines_estimations.csv";
	boolean dailyvaccestimation = false;
	double[][] nvacday;
	
	// 10) ADAPTATIVE CONTROL MEASURES
	boolean adaptative = false;
	int currentncm; // Current number of different control measures or different levels of control measures
	int nth = 2;    // Number of thresholds
	double[] threshold = new double[nth];
	double[] thm       = new double[nth];
	double[] thkappa   = new double[nth];
	
	// 11) TO ALLOW CONSIDERING TIME DEPENDENT GAMMAS
	double[][] sX;
	//double[]   vjRKg;
	
	
	public Country(String iname){
		path = currentFolder + "\\Scenarios\\" +iname+"\\";
		outputpath = currentFolder + "\\Output\\" +iname+"\\";
		readDisease();
		numerrors= 4;
		dt=1.0/6.0;
		setSimulation(datei, datef, dt);
		sX = computeSX();
	}
	
	public Country(String iname, double population, int inerrors){
		totalpop = population;
		name = iname;
		numerrors= inerrors;
		path = currentFolder + "\\Scenarios\\" +iname+"\\";
		outputpath = currentFolder + "\\Output\\" +iname+"\\";
	}
	
	public Country(String cFolder, String iname, double population, int inerrors){
		currentFolder = cFolder;
		totalpop = population;
		name = iname;
		numerrors= inerrors;
		path = currentFolder + "\\Scenarios\\" +iname+"\\";
		outputpath = currentFolder + "\\Output\\" +iname+"\\";
	}
	
	public void setSimulation(String idateini, String idatefin, double idt){
		dt    = idt;
		datei = idateini;
		datef = idatefin;
		try {
			dateinit = format.parse(datei);
			datefin  = format.parse(datef);
			// dmax: Number of days between datei and datef
			dmax = Math.round(datefin.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24))+1;
			
			for(int i=0;i<nvaccines;i++){
				Date datevacc = format.parse(dateinitvacc[i]);
				initvacc[i]   = Math.round(datevacc.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24));
			}
			
			for(int i=0;i<csvdatesch.length;i++){
				if(dursuddench[i]==true){
					Date datech         = format.parse(csvdatesch[i]);				
					tstepdurch[i]       = (int)((Math.round(datech.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24)))/dt);
					tstepdurchlin[2*i]  = 0;
					tstepdurchlin[2*i+1]= 0;
				}
				else if(durlinch[i]==true){
					Date datech         = format.parse(csvdateschlin[2*i]);				
					tstepdurchlin[2*i]  = (int)((Math.round(datech.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24)))/dt);
					datech              = format.parse(csvdateschlin[2*i+1]); 
					tstepdurchlin[2*i+1]= (int)((Math.round(datech.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24)))/dt);
					tstepdurch   [i]    = 0;   
				}
				else{
					tstepdurchlin[2*i]  = 0;
					tstepdurchlin[2*i+1]= 0;
					tstepdurch   [i]    = 0; 
				}
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		readTimeSeries();
	    computeNormRepData();
	    readControlMeasures();
	    setTimesTheta();
	}
	
	public void setLambdas(String[] idatecm){
		int givencm = idatecm.length;
		for (int i=0; i<givencm; i++){
			datecm[i] = idatecm[i];
		}
		while (ncm-givencm>0){
			datecm[givencm] = "01-Jan-2050";
			givencm++;
		}
		try {
			for (int i=0; i<ncm; i++){
				Date datel = format.parse(datecm[i]);
				lambda[i] = (int)((Math.round(datel.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24)))/dt); 
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setTimesTheta(){
		timestheta = new int[2];
		for (int id=1; id<Nhist; id++){
	    		if((repdata[id][1]>0)&&(repdata[id-1][1]==0)){
	    			timestheta[0] = (int) (id/dt);
	    			break;
	    		}
		}
		timestheta[1] = (int) ((NhistC-1)/dt);
	}
	
	public void setTimesTheta(String idatetheta){
		timestheta = new int[2];
		for (int id=1; id<Nhist; id++){
    		if((repdata[id][1]>0)&&(repdata[id-1][1]==0)){
    			timestheta[0] = (int) (id/dt);
    			break;
    		}
		}
		Date datet;
		try {
			datet = format.parse(idatetheta);
			timestheta[1] = (int)((Math.round(datet.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24)))/dt); 
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setTimesTheta(String[] idatetheta){
		int n = idatetheta.length;
		timestheta = new int[n];
		try {
			for (int i=0; i<n; i++){
				Date datet = format.parse(idatetheta[i]);
				timestheta[i] = (int)((Math.round(datet.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24)))/dt); 
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void readTimeSeries(){
		BufferedReader br = null;
		try {
			// 1) Find out the number of historical data (Nhist) and 
			//    the number of available time series (numseries)
			br = new BufferedReader(new FileReader(path+tsfile));
			
			String line = br.readLine();
			String[] vline = line.split(SEPARATOR); 
			numseries = vline.length;
			int numlin = 0; // Starting in 0 to not count the first line which contains the description of each column
			while((br.readLine())!=null){
				numlin++;
			}
			Nhist = numlin; //Nhist = Number of historical data
			br.close();
			
			// 2) Assign size to data vectors and fill them
			dates  = new String[Nhist];
			repdata = new double[Nhist][numseries-1];
			
			br = new BufferedReader(new FileReader(path+tsfile));
			
			line = br.readLine(); // the first line is not used
			int cntund = 0; // to count the number of undetected deaths data
			tund   = new int   [cntund+1];
			repUnD = new double[cntund+1];
			for (int id=0; id<Nhist; id++){
				line = br.readLine();
				vline = line.split(SEPARATOR); 
				dates[id]= vline[0];
				for (int k=1; k<numseries; k++){
					repdata[id][k-1]= Double.parseDouble(vline[k]);
				}
				if (dates[id].equals(datei)){
	        		firstIndex = id; 
	        		repUnD[cntund] = repdata[id][7+nvariants];
	        		Date datet;
					try {
						datet = format.parse(dates[id]);
						tund[cntund] = (int)((Math.round(datet.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24)))/dt); 
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cntund++;
	        	}
				else if (repdata[id][7+nvariants]>0){
					tund   = new int   [cntund+1];
					repUnD = new double[cntund+1];
					repUnD[cntund] = repdata[id][7+nvariants];
					Date datet;
					try {
						datet = format.parse(dates[id]);
						tund[cntund] = (int)((Math.round(datet.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24)))/dt); 
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cntund++;
	        	}
			}
			br.close();
			NhistC = Nhist-firstIndex;
			
			coefmort = new double[dmax];
			coefbetaH= new double[dmax];
			propvar  = new double[dmax][nvariants];
			nvacday  = new double[nvaccines][dmax];
			File extravarfile = new File(path+coefmortfile);
		    if (! extravarfile.exists()){
		    	dailyvaccestimation = false;
		        for(int ti=0;ti<dmax;ti++){
		        	coefmort [ti]   =1.0;
		        	coefbetaH[ti]   =1.0;
		        	propvar  [ti][0]=1.0;
		        	for(int i=1;i<nvariants;i++){
						propvar[ti][i]=0.0;
					}
		        }
		    }
		    else{
				br = new BufferedReader(new FileReader(path+coefmortfile));
				line = br.readLine();
				numlin = 0;
				line = br.readLine();
				while(line!=null){
					vline = line.split(SEPARATOR);
					coefmort [numlin]=Double.parseDouble(vline[1]);
					coefbetaH[numlin]=Double.parseDouble(vline[2]);
					for(int i=0;i<nvariants;i++){
						propvar[numlin][i]=Double.parseDouble(vline[3+i])/100.0;
					}
					for(int j=0;j<nvaccines;j++){
						if((!vline[3+nvariants+j].equals("-"))&&(!vline[3+nvariants+j].equals(""))){
							nvacday[j][numlin]=Double.parseDouble(vline[3+nvariants+j]);
							dailyvaccestimation = true;
						}
						else{
							nvacday[j][numlin]=0.0;
						}
					}
					line = br.readLine();
					numlin++;
				}
		    }
			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void computeNormRepData(){
		// FOR COMPARISON TO REALITY ----------------------------------------------------------------------------
		double[][] cuadRepData = new double[numerrors-1][NhistC];
		normRepData = new double[numerrors-1];
		// Number of the time step that should be compared
		tstepc = new int[NhistC];
		timec = new double[NhistC]; 
		try {
			for (int j=0; j<NhistC; j++){
				
				tstepc[j] = (int) ((Math.round((format.parse(dates[firstIndex+j])).getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24)))/dt);
				
				timec[j] = tstepc[j] * dt;
				for (int k=0; k<numerrors-1; k++){
					cuadRepData[k][j] = repdata[firstIndex+j][k]*repdata[firstIndex+j][k];
				}
			}
		} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		for (int k=0; k<numerrors-1; k++){
			normRepData[k] = trapz(timec,cuadRepData[k]);
		}
		
		// UNDETECTED DEATHS: 
		timecund = new double[tund.length]; 
		double[] cuadRepDataUnD = new double[tund.length];
		for(int j=0; j<tund.length; j++){
			timecund[j] = tund[j] * dt; 
			cuadRepDataUnD[j] = repUnD[firstIndex+j]*repUnD[firstIndex+j];
		}
		normRepDataUnD = trapz(timecund,cuadRepDataUnD);
	}
	
	public void readControlMeasures(){
		BufferedReader br = null;
		try {
			// 1) Find out the number of different control measures or different levels of control measures(ngivencm)
			//    and fill data vectors 
			csvm    [0]=1.0;
			csvkappa[0]=0.0;
			int ngivencm;
			br = new BufferedReader(new FileReader(path+cmfile));
			
			String line = br.readLine(); // the first line is not used
			line = br.readLine(); 
			int numlin = 0; // Starting in 0 to not count the first line which contains the description of each column
			while((line!=null)&&(!line.contains("adaptative"))){
				String[] vline  = line.split(SEPARATOR); 
				datecm  [numlin]= vline[0];
				csvm    [numlin+1]= Double.parseDouble(vline[1]);
				csvkappa[numlin+1]= Double.parseDouble(vline[2]);
				line = br.readLine();
				numlin++;
			}
			ngivencm = numlin; 
			currentncm = ngivencm;
			
			// 2) Fill the remaining dates until compete the maximum control measure dates ncm
			while (ncm-ngivencm>0){
				datecm[ngivencm] = "01-Jan-2050";
				ngivencm++;
			}
			
			// NEW) ADAPTATIVE
			if((line!=null)&&(line.contains("adaptative"))){
				adaptative = true;
				String[] vline  = line.split(SEPARATOR); 
				datecm  [numlin]= vline[0];
				for(int th=0;th<nth;th++){
					line = br.readLine();
					vline  = line.split(SEPARATOR);
					threshold[th]=Double.parseDouble(vline[0]);
					thm      [th]=Double.parseDouble(vline[1]);
					thkappa  [th]=Double.parseDouble(vline[2]);
				}
			}
			br.close();
			
			// 3) Compute lambdas
			try {
				for (int i=0; i<ncm; i++){
					Date datel = format.parse(datecm[i]);
					lambda[i] = (int)((Math.round(datel.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24)))/dt); 
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void readDisease(){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path+dfile));
			
			// 1st line: Name;Input_disease_name;Input_country_name;;;;;
			String line    = br.readLine(); 
			String[] vline = line.split(SEPARATOR);
			name = vline[2];
			
			// 2nd line: Country population;input_country_pop;;;;;;
			line  = br.readLine(); 
			vline = line.split(SEPARATOR);
			totalpop = Double.parseDouble(vline[1]);
			
			// 3rd line: Initial and final dates;input_initdate;input_final_date;;;;;
			line  = br.readLine(); 
			vline = line.split(SEPARATOR);
			datei = vline[1];
			datef = vline[2];
			
			// 4th line: Date of sudden changes in durations
			line  = br.readLine(); 
			vline = line.split(SEPARATOR);
			int nchanges = 0; // counter for sudden changes
			int nlinearch= 0; // counter for linear changes
			for(int i=0;i<csvdatesch.length; i++){
				if((vline.length>1)&&(!vline[i+1].equals(""))){
					if((vline[i+1].equals("linear"))){
						nlinearch++;
						durlinch[i]=true;
					}
					else{
						nchanges++;
						csvdatesch[i] =vline[i+1];
						dursuddench[i]=true;
					}
				}
			}
			
			// 5th* line (it only appears if there is some linear change in 4th line: 
			// Starting and final date of linear changes in durations;
			if(nlinearch!=0){
				line  = br.readLine(); 
				vline = line.split(SEPARATOR);
				nlinearch= 0; // counter for linear changes
				for(int i=0;i<csvdatesch.length; i++){
					if((durlinch[i]==true)){
						nlinearch++;
						csvdateschlin[2*i]   = vline[1+2*(nlinearch-1)];
						csvdateschlin[2*i+1] = vline[2+2*(nlinearch-1)];
					}
				}
			}
			
			// 5th line: Durations;input_dure;input_dure;input_dure;input_dure;input_dure;input_dure;input_changedur1;input_changedur2;...
			csvdur = new double[numstates-1+nchanges+nlinearch];
			line  = br.readLine(); 
			vline = line.split(SEPARATOR);
			for(int i=0;i<csvdur.length; i++){
				csvdur[i]=Double.parseDouble(vline[i+1]);
			}
			
			// 6th line: BetaI;input_betaI;;;;;;
			line  = br.readLine(); 
			vline = line.split(SEPARATOR);
			csvbetai = Double.parseDouble(vline[1]);
			
			// 7th line: Coef Beta;input_coefE;input_coefIu;;;;;
			line  = br.readLine(); 
			vline = line.split(SEPARATOR);
			csvcoef[0] = Double.parseDouble(vline[1]);
			csvcoef[1] = Double.parseDouble(vline[2]);
			
			// 8th line: FR;input_omegamin;input_omegamax;input_omegau;;;;
			line  = br.readLine(); 
			vline = line.split(SEPARATOR);
			csvFR[0] = Double.parseDouble(vline[1]);
			csvFR[1] = Double.parseDouble(vline[2]);
			csvFR[2] = Double.parseDouble(vline[3]);
			
			// 9th line: p;input_p;;;;;;
			line  = br.readLine(); 
			vline = line.split(SEPARATOR);
			csvp  = Double.parseDouble(vline[1]);
			
			// 10th line: k;input_k for variant 2;input_k for variant 3;input_k for variant 4;...;;;
			//            fatality increment for variant 2;fatality increment for variant 3;fatality increment for variant 4;...;;;
			line  = br.readLine(); 
			if (line!=null){
				vline = line.split(SEPARATOR);
				nvariants = nvariants+(int)Math.floor(vline.length/2);
				csvk  = new double[nvariants-1];
				csvfr = new double[nvariants-1];
				for(int i=0; i<nvariants-1; i++){
					csvk [i] = Double.parseDouble(vline[1+i]);
					if(nvariants+i<vline.length){
						csvfr[i] = Double.parseDouble(vline[nvariants+i]);
					}
					else{
						csvfr[i] = 0.0;
					}
				}
	
				// 11th line: vaccines;nvaccines;date init Vaccine_1; ...;date init Vaccine_nvaccines;  
				line  = br.readLine(); 
				vline = line.split(SEPARATOR);
				nvaccines= Integer.parseInt(vline[1]);
				dateinitvacc = new String[nvaccines];
				initvacc = new int[nvaccines];
				for(int i=0;i<nvaccines; i++){
					dateinitvacc[i] = vline[2+i];
				}
				
				// 12th line: days 2nd dose; days 2nd dose vaccine 1; days 2nd dose vaccine 2; ...;
				days2nd = new int[nvaccines];
				line  = br.readLine(); 
				vline = line.split(SEPARATOR);
				for(int i=0;i<nvaccines; i++){
					days2nd[i] = Integer.parseInt(vline[i+1]);
				}
				
				// Following lines: 
				//e_j (efficacy of Vaccine_j) ;;...;;;
				//days e_j (days efficacy;
				//
				efficacy     = new double[nvaccines][nmaxeff];
				daysefficacy = new int[nvaccines][nmaxeff];
				nvacweek     = new int[nvaccines][nmaxweeks];
				for(int i=0; i<nvaccines; i++){
					line  = br.readLine(); 
					vline = line.split(SEPARATOR);
					for (int j=1; j<vline.length; j++){
						efficacy[i][j-1] = Double.parseDouble(vline[j]);
					}
					line  = br.readLine(); 
					vline = line.split(SEPARATOR);
					for(int j=1; j<vline.length; j++){
						daysefficacy[i][j-1] = Integer.parseInt(vline[j]);
					}
					//   nvacweek
					line  = br.readLine(); 
					vline = line.split(SEPARATOR);
					for(int j=1;j<=nmaxweeks; j++){
						if(j<vline.length){
							nvacweek[i][j-1] = Integer.parseInt(vline[j]);
						}
						else{
							nvacweek[i][j-1] = nvacweek[i][vline.length-2];
						}
					}
				}
			}
			line  = br.readLine();
			while (line!=null){
				if((line.contains("t_Rd"))){
					vline      = line.split(SEPARATOR);
					tRdvacdays = Integer.parseInt(vline[1]);
				}
				line  = br.readLine();
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public double[] readFromFile(String fileIorE){
		double[] result = new double[Nhist];
		File archivo     = new File(path + fileIorE);
		if (archivo.exists()){
			FileReader fr    = null;
			BufferedReader br= null;
			try {	
				fr    = new FileReader(archivo);
				br    = new BufferedReader(fr);
				for (int id=0; id<Nhist; id++){
					result[id]= Double.parseDouble(br.readLine());
		        }
				fr.close();
			}
			catch(Exception e){
				e.printStackTrace();
	        }finally{
	           try{  
	        	   if (null!= fr){
	        		   fr.close();
	        	   }
	           }catch (Exception e2){ 
	              e2.printStackTrace();
	           }
	        }
		}
		return result;
	}
	
	public double[][] computeSX(){
		int tmax = (int) ((dmax-1)/dt)+1; // Number of model time steps
		int thistC = (int) ((NhistC-1)/dt)+1; // Number of model time steps (reported data)
		int thistmax= Math.max(tmax, thistC);
		double[][] sXt = new double[numstates-2][thistmax];
		// sX={sE, sI, sIu, sHR, sHD};
		double T1, T2, t1, t2, g1, g2, coefcte, intT1T2;
		int counterch=0;
		for(int state=0;state<sXt.length;state++){
			if(durlinch[state]==true){
				t1 = tstepdurchlin[2*state]  *dt; //DAYS
				t2 = tstepdurchlin[2*state+1]*dt; //DAYS
				g1 = 1.0/csvdur[state];
				g2 = 1.0/csvdur[numstates-1+counterch];
				double di = dt/10.0; // Step to have a thinner discretization in the integral computation
				coefcte = (g2-g1)/(2.0*(t2-t1));
				for(int t=0; t<sXt[0].length; t++){
					T1 = Math.max(0.0, t1-t*dt); //DAYS
					T2 = Math.max(0.0, t2-t*dt); //DAYS
					double[] timeInt = new double[(int) ((T2-T1)/di+1)];
					double[] fInt    = new double[(int) ((T2-T1)/di+1)];
					for(int i=0;i<timeInt.length; i++){ // Integral 
						timeInt[i]= T1+i*di; //DAYS
						fInt   [i]= Math.exp(-(coefcte*Math.pow(t*dt+timeInt[i]-t1, 2)+g1*timeInt[i]));
					}
					intT1T2 = simpson(timeInt,fInt);
					sXt[state][t] = (1.0/g1)*(1-Math.exp(-g1*T1))+Math.exp(coefcte*Math.pow(t*dt+T1-t1,2))*intT1T2+(1.0/g2)*Math.exp(coefcte*(Math.pow(t*dt+T1-t1, 2)-Math.pow(t*dt+T2-t1, 2))-g1*T2);
				}
				counterch++;
			}
			else if(dursuddench[state]==true){
				t1 = tstepdurch[state]*dt; //DAYS
				g1 = 1.0/csvdur[state];
				g2 = 1.0/csvdur[numstates-1+counterch];
				for(int t=0; t<sXt[0].length; t++){
					T1 = Math.max(0.0, t1-t*dt); //DAYS
					sXt[state][t] = (1.0/g1)*(1-Math.exp(-g1*T1))+(1.0/g2)*Math.exp(-g1*T1);
				}
				counterch++;
			}
			else{
				for(int t=0; t<sXt[0].length; t++){
					sXt[state][t] = csvdur[state];
				}
			}
		}
		return sXt;
	}
	
	public double[] evaluate (double delay, double[] dur, double varduri, double betai, double[] coef, double[] kappa, double[] m, double[] kappasanit, double[] msanit, double[] omega, double pcte) {
		
		double [] relerror = new double[numerrors] ; // Relative error committed in each time series
		
		// Matrix initialization --------------------------------------------------------------------------
		int tmax = (int) ((dmax-1)/dt)+1; // Number of model time steps
		int thistC = (int) ((NhistC-1)/dt)+1; // Number of model time steps (reported data)
		int thistmax= Math.max(tmax, thistC);
		int delayini=(int)(delay/dt);
		double[][][] states  = new double[tmax][numstates][nvariants]; // initialize states matrix
		//states[t] = {S[t],E[t],I[t],Iu[t],HR[t], HD[t], Q[t]};
		double[] H  = new double[tmax]; // initialize Hospitalized matrix (H=HR+HD)
		double[] Q  = new double[tmax]; // initialize Quarantine matrix (Q = states[:][numstates-1])
		double[] R  = new double[tmax]; // initialize Recovered matrix
		double[] D  = new double[tmax]; // initialize Death matrix
		double[] Du = new double[tmax]; // initialize Death Undetected matrix
		double[] Ru = new double[tmax]; // initialize Recovered Undetected matrix
		double[] Iu = new double[tmax]; // initialize Infectious Undetected matrix
		double[] CC = new double[tmax]; // initialize Cumulative Cases matrix
		double[] CD = new double[tmax]; // initialize Cumulative Deaths matrix
		double[][] V= new double[nvaccines][tmax]; // initialize Immune Vaccinated matrix
		double[] CI14=new double[tmax]; // 14-days Cumulative Incidence 
		//-----------------------------------------------------------------------------------------------
		
		// Matrix initialization for PARAMETERS DEPENDING ON TIME ----------------------------------------
		// multiplicamos por dos el tiempo para poder hacer RK4 en t+0.5
		double[][][] beta = new double[2*tmax+1][numstates-2][nvariants];  // beta = {mbetae, mbetai, mbetaiu, mbetahr, mbetahd}
		double[][] gamma  = new double[2*thistmax+1][numstates-1]; // gamma= {gammae, gammai, gammaiu, gammahr, gammahd, gammaq}
		double[][] cmeasures= new double[2*thistmax+1][2]; // cmeasures = {SOCIAL control measures mc(t), SANITARY control measures mcsanit(t)}
		double[][] frate 	= new double[2*thistmax+1][nvariants]; // frate=OMEGA (fatality rate)
		double[][] theta    = new double[2*tmax+1][nvariants];
		double[][] p        = new double[2*tmax+1][nvariants];
		double[] omegau  = new double[2*tmax+1];//omega[2];
		//-----------------------------------------------------------------------------------------------
		
		// To compare with reported data-----------------------------------------------------------------
		double[][] difdata   = new double[numerrors-1][NhistC];
		double[][] difpenal  = new double[numerrors-1][NhistC];
		double[] normdif     = new double[numerrors-1];
		double[] normdifpenal= new double[numerrors-1];
		double ndifpenlast   = 0.0;
		// ----------------------------------------------------------------------------------------------
		
		// STATES INITIALIZATION -------------------------------------------------------------------------------
		//states[delayini][1][0] = repdata[(int) Math.floor(delayini*dt)][6];//repdata[t][6] = datoImpE[t];
		//states[delayini][1][0] = repdata[(int) Math.floor(delayini*dt)][6];//repdata[t][6] = datoImpE[t];
		double sumE = 0.0;
		for(int i=0;i<nvariants;i++){
			states[delayini][1][i] = repdata[(int) Math.floor(delayini*dt)][6+i];//repdata[t][6] = datoImpE[t];
			sumE = sumE + states[delayini][1][i];
		}
		states[delayini][0][0] = totalpop - sumE;
		//----------------------------------------------------------------------------------------------------------
		double[][] vaccinated = new double[nvaccines][thistmax];
		double[][] vj         = new double[nvaccines][thistmax];
		double[]   vjsum      = new double[thistmax];
		double[]   difvac     = new double[thistmax];
		double[][] sumvac     = new double[nvaccines][thistmax];
		double[]   Fvac		  = new double[thistmax];
		double[]   ASvac      = new double[thistmax];
		double[][] intAS      = new double[nvaccines][thistmax];
		double[][] vac2ndDose = new double[nvaccines][thistmax];
		double[][] sumvac2ndD = new double[nvaccines][thistmax];
		// Vaccinated
		for (int j=0; j<nvaccines; j++){
			if(numseries-1>8+nvariants+j){
				vaccinated[j][delayini] = repdata[(int) Math.floor(delayini*dt)][8+nvariants+j];
			}
			else if(dailyvaccestimation==false){ 
				for(int i=0;i<nvacweek[j].length;i++){
					if((int) Math.floor(delayini*dt)>=initvacc[j]+7*i){
						vaccinated[j][delayini] = nvacweek[j][i]/7.0;
					}
				}
			}
			else{
				vaccinated[j][delayini] = nvacday[j][(int) Math.floor(delayini*dt)];
			}
		}
		
		// STRATEGY TO COMPUTE THETA:
		for (double t=delayini; t<thistmax+0.5; t=t+0.5){
			cmeasures = evaluateControlMeasures(cmeasures, t, kappa, m, kappasanit, msanit);
			gamma[(int)(2*t)] = evaluateGamma(t, dur, cmeasures[(int)(2*t)][1], varduri);
			frate[(int)(2*t)][0] = cmeasures[(int)(2*t)][1]*omega[1] + (1-cmeasures[(int)(2*t)][1])*omega[0];
			frate[(int)(2*t)][0] = frate[(int)(2*t)][0]*coefmort[(int)Math.floor(t*dt)];
			for(int i=1; i<nvariants; i++){
				frate[(int)(2*t)][i] = (1+csvfr[i-1])*frate[(int)(2*t)][0];
			}
		}
		double[] repfrateef = computeCFR();//(gamma);
		//----------------------------------------------------------------------------------------------------------
		
		double[] etat = interpLineal(computeEta((int)Math.round(1/gamma[0][0]+1/gamma[0][1])));
		
		// CALCULO PARAMETROS EN t inicial (se guardan en la posición 2*delayini)-----------------------------------
		int tthetai = (int) ((repfrateef[NhistC]+6)/dt);// tthetaini = cfr[NhistC]+6
		double evaltheta = evaluateTheta(delayini, delayini, getColumn(frate,0), repfrateef);
		double coeftheta = propvar[(int)(Math.floor(delayini*dt))][0];
		for(int i=1; i<nvariants; i++){
			coeftheta = coeftheta + (1+csvfr[i-1])*propvar[(int)(Math.floor(delayini*dt))][i];
		}
		for(int i=0; i<nvariants; i++){
			theta[2*delayini][i] = Math.min(1.0,coeftheta*evaltheta);
			p    [2*delayini][i] = evaluateP(pcte, theta[2*delayini][i], theta[2*delayini][i], frate[2*tthetai][i], frate[2*delayini][i]);
		}
		omegau[2*delayini]= evaluateOmegaU(delayini, omega[2]);
		beta  [2*delayini]= evaluateBeta  (delayini, betai, coef, cmeasures[2*delayini], gamma[2*delayini], frate[2*delayini][0], theta[2*delayini][0], p[2*delayini][0], omegau[2*delayini], etat[2*delayini]);
		//----------------------------------------------------------------------------------------------------------
		
		for(int t=delayini; t<tmax-1; t++){
			
			// CALCULO PARAMETROS EN t+0.5 (se guardan en la posición 2*t+1)------------------------------------------
			evaltheta = evaluateTheta(t, t+0.5, getColumn(frate,0), repfrateef);
			coeftheta = propvar[(int)(Math.floor((t+0.5)*dt))][0];
			for(int i=1; i<nvariants; i++){
				coeftheta = coeftheta + (1+csvfr[i-1])*propvar[(int)(Math.floor((t+0.5)*dt))][i];
			}
			for(int i=0; i<nvariants; i++){
				theta[2*t+1][i] = Math.min(1.0,coeftheta*evaltheta); 
				p    [2*t+1][i] = evaluateP(pcte, theta[2*delayini][i], theta[2*t+1][i], frate[2*tthetai][i], frate[2*t+1][i]);
			}
			omegau[2*t+1]= evaluateOmegaU(t+0.5, omega[2]);
			beta  [2*t+1]= evaluateBeta  (t+0.5, betai, coef, cmeasures[2*t+1], gamma[2*t+1], frate[2*t+1][0], theta[2*t+1][0], p[2*t+1][0], omegau[2*t+1], etat[2*t+1]);
			//----------------------------------------------------------------------------------------------------------
			
			// CALCULO PARAMETROS EN t+1 (se guardan en la posición 2*(t+1))------------------------------------------
			evaltheta = evaluateTheta(t, t+1, getColumn(frate,0), repfrateef);
			coeftheta = propvar[(int)(Math.floor((t+1)*dt))][0];
			for(int i=1; i<nvariants; i++){
				coeftheta = coeftheta + (1+csvfr[i-1])*propvar[(int)(Math.floor((t+1)*dt))][i];
			}
			for(int i=0; i<nvariants; i++){
				theta[2*t+2][i] = Math.min(1.0,coeftheta*evaltheta); 
				p    [2*t+2][i] = evaluateP(pcte, theta[2*delayini][i], theta[2*t+2][i], frate[2*tthetai][i], frate[2*t+2][i]);
			}
			omegau[2*t+2]= evaluateOmegaU(t+1, omega[2]);
			beta  [2*t+2]= evaluateBeta  (t+1, betai, coef, cmeasures[2*t+2], gamma[2*t+2], frate[2*t+2][0], theta[2*t+2][0], p[2*t+2][0], omegau[2*t+2], etat[2*t+2]);
			
			for (int j=0; j<nvaccines; j++){
				for(int k=1; k<efficacy[j].length;k++){
					if((((int) Math.floor((t+1)*dt))>=daysefficacy[j][k])&&(efficacy[j][k]>0)){
						vj[j][t+1] = vj[j][t+1] + (efficacy[j][k]-efficacy[j][k-1])*vaccinated[j][(int) ((t+1)-(daysefficacy[j][k]/dt))]*(ASvac[(int) ((t+1)-(daysefficacy[j][k]/dt))]/Fvac[(int) ((t+1)-(daysefficacy[j][k]/dt))]);
					}
				}
				vjsum[t+1] = vjsum[t+1] + vj[j][t+1];
			}
			//----------------------------------------------------------------------------------------------------------

			double[][] fsyseval = systemf(t, states[t], beta[2*t], gamma[2*t], frate[2*t], theta[2*t], p[2*t], omegau[2*t], vjsum);
			
			// RUNGE KUTTA 4 etapas y orden 4 
			double[][] statesF2 = new double [numstates][nvariants];
			for (int s=0; s<numstates; s++){
				for (int i=0; i<nvariants; i++){
					statesF2[s][i] = states[t][s][i] + dt/2*fsyseval[s][i];
				}
			}

			double[][] fsysevalF2 = systemf(t+0.5, statesF2, beta[2*t+1], gamma[2*t+1], frate[2*t+1], theta[2*t+1], p[2*t+1], omegau[2*t+1], vjsum);
			
			double[][] statesF3 = new double [numstates][nvariants];
			for (int s=0; s<numstates; s++){
				for (int i=0; i<nvariants; i++){
					statesF3[s][i] = states[t][s][i] + dt/2*fsysevalF2[s][i];
				}
			}
			  
			double[][] fsysevalF3 = systemf(t+0.5, statesF3, beta[2*t+1], gamma[2*t+1], frate[2*t+1], theta[2*t+1], p[2*t+1], omegau[2*t+1], vjsum);
			
			double[][] statesF4 = new double [numstates][nvariants];
			for (int s=0; s<numstates; s++){
				for (int i=0; i<nvariants; i++){
					statesF4[s][i] = states[t][s][i] + dt*fsysevalF3[s][i];
				}
			}
			
			double[][] fsysevalF4 = systemf(t+1, statesF4, beta[2*t+2], gamma[2*t+2], frate[2*t+2], theta[2*t+2], p[2*t+2], omegau[2*t+2], vjsum);
			
			for (int s=0; s<numstates; s++){
				for (int i=0; i<nvariants; i++){
					states[t+1][s][i] = states[t][s][i] + dt/6*(fsyseval[s][i]+2*fsysevalF2[s][i]+2*fsysevalF3[s][i]+fsysevalF4[s][i]);
				}
			}
			
			D[t+1] = D[t];
			Du[t+1] = Du[t];
			CC[t+1] = CC[t];
			for (int i=0; i<nvariants; i++) {
				H[t+1] = H[t+1] + states[t+1][4][i] + states[t+1][5][i]; // 4 is HR and 5 is HD
				Q[t+1] = Q[t+1] + states[t+1][6][i]; // 6 is Q
				Iu[t+1]= Iu[t+1]+ states[t+1][3][i]; // 3 is Iu
				D[t+1] = D[t+1] + dt/2*(gamma[2*t][4]*states[t][5][i]+gamma[2*(t+1)][4]*states[t+1][5][i]); // HD = states[5]; gammahd = gamma[4]
				Du[t+1]= Du[t+1]+ dt/2*(omegau[2*t]*gamma[2*t][1]*states[t][2][i]+omegau[2*(t+1)]*gamma[2*(t+1)][1]*states[t+1][2][i]); 
				CC[t+1]= CC[t+1]+ dt/2*(theta[2*t][i]*gamma[2*t][1]*states[t][2][i]+theta[2*(t+1)][i]*gamma[2*(t+1)][1]*states[t+1][2][i]); // I= states[2]; gammai = gamma[1]
			}
			R [t+1]= R [t] + dt/2*(gamma[2*t][5]*Q[t] + gamma[2*(t+1)][5]*Q[t+1]);  // gammaq=gamma[5]
			Ru[t+1]= Ru[t] + dt/2*(gamma[2*t][2]*Iu[t]+ gamma[2*(t+1)][2]*Iu[t+1]); // gammaIu=gamma[2]
			CD[t+1]= D [t+1];
			
			
			if(((t+1)%(1/dt)==0)&&((t+1)*dt<NhistC)) {
				for(int i=0; i<nvariants; i++){
					states[t+1][1][i]=states[t+1][1][i]+repdata[(int) Math.floor((t+1)*dt)][6+i]; //repdata[t][6] = datoImpE[t];
				}
				states[t+1][4][0]=states[t+1][4][0]-repdata[(int) Math.floor((t+1)*dt)][6+nvariants]; //repdata[t][7] = datoEvac[t];
			}
			
			// Vaccinated
			double auxsumEi=0.0;
			ASvac [t] = states[t][0][0]; 
			for (int j=0; j<nvaccines; j++){
				for(int i=0; i<nvariants; i++){ 
					auxsumEi= auxsumEi + states[t][1][i]; 
				}
				Fvac  [t]     = states[t][0][0]+auxsumEi+R[Math.max((int) (t-tRdvacdays/dt),0)]+Ru[t]- difvac[t];
				ASvac [t]     = ASvac [t] -(intAS[j][Math.max(t-1, 0)]-V[j][t]);
				intAS [j][t]  = intAS [j][Math.max(t-1, 0)] + dt*(ASvac[Math.max(t-1, 0)]*vaccinated[j][Math.max(t-1, 0)]/Fvac[Math.max(t-1, 0)]);
			}
			
			if((ASvac[t]<0)||(Fvac[t]<=0)){
				ASvac [t]= 0.0;
				difvac[t]= 0.0;
				for (int j=0;j<nvaccines;j++){
					vaccinated[j][t] = 0.0;
					sumvac    [j][t] = sumvac[j][Math.max(t-1, 0)] + dt/2*(vaccinated[j][Math.max(t-1, 0)]+vaccinated[j][t]);
					difvac       [t] = difvac[t]  +(sumvac[j][t]-V[j][t]);
				}
			}
			
			if(((t+1)*dt<NhistC)) {
				for (int j=0;j<nvaccines;j++){
					if(numseries-1>8+nvariants+j){
						vaccinated[j][t+1] = repdata[(int) Math.floor((t+1)*dt)][8+nvariants+j];
						vac2ndDose[j][t+1] = vaccinated[j][Math.max((int) ((t+1)-days2nd[j]/dt),0)];
					}
					else if(dailyvaccestimation==false){ 
						for(int i=0;i<nvacweek[j].length;i++){
							if((int) Math.floor((t+1)*dt)>=initvacc[j]+7*i){
								if((int) Math.floor((t+1)*dt)<initvacc[j]+days2nd[j]){ 
									vaccinated[j][t+1] = nvacweek[j][i]/7.0;
								}
								else if((i<2)||(nvacweek[j][i]>=nvacweek[j][i-2])||((int) Math.floor((t+1)*dt)>=initvacc[j]+7*nvacweek[j].length)){
									vaccinated[j][t+1] = Math.max((nvacweek[j][i]/7.0)-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
								}
								else{
									vaccinated[j][t+1] = Math.max((nvacweek[j][i]/7.0)-(vaccinated[j][(int) ((t+1)-(days2nd[j]-14)/dt)]-(nvacweek[j][i]/7.0))-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
								}
							}
						}
					}
					else{
						if((int) Math.floor((t+1)*dt)<initvacc[j]+days2nd[j]){ 
							vaccinated[j][t+1] = nvacday[j][(int) Math.floor((t+1)*dt)];
						}
						else{
							vaccinated[j][t+1] = Math.max(nvacday[j][(int) Math.floor((t+1)*dt)]-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
							vac2ndDose[j][t+1] = vaccinated[j][(int) ((t+1)-days2nd[j]/dt)];
						}
					}
				}
			}
			else {
				if(dailyvaccestimation==false){
				for (int j=0; j<nvaccines; j++){
					for(int i=0;i<nvacweek[j].length;i++){
						if((int) Math.floor((t+1)*dt)>=initvacc[j]+7*i){
							if((int) Math.floor((t+1)*dt)<initvacc[j]+days2nd[j]){
								vaccinated[j][t+1] = nvacweek[j][i]/7.0;
							}
							else if((i<2)||(nvacweek[j][i]>=nvacweek[j][i-2])||((int) Math.floor((t)*dt)>=initvacc[j]+7*nvacweek[j].length)){
								vaccinated[j][t+1] = Math.max((nvacweek[j][i]/7.0)-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
							}
							else{
								vaccinated[j][t+1] = Math.max((nvacweek[j][i]/7.0)-(vaccinated[j][(int) ((t+1)-(days2nd[j]-14)/dt)]-(nvacweek[j][i]/7.0))-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
							}
						}
					}
				}
				}
				else{
					for (int j=0; j<nvaccines; j++){
						if((int) Math.floor((t+1)*dt)<initvacc[j]+days2nd[j]){
							vaccinated[j][t+1] = nvacday[j][(int) Math.floor((t+1)*dt)];
						}
						else{
							vaccinated[j][t+1] = Math.max(nvacday[j][(int) Math.floor((t+1)*dt)]-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
							vac2ndDose[j][t+1] = vaccinated[j][(int) ((t+1)-days2nd[j]/dt)];
						}
					}
				}
			}
			
			for (int j=0; j<nvaccines; j++){
				V     [j][t+1]= V  [j][t]    + dt/2*(vj[j][t] + vj[j][t+1]);
				sumvac[j][t+1]= sumvac[j][t] + dt/2*(vaccinated[j][t]+vaccinated[j][t+1]);
				difvac   [t+1]= difvac[t+1]  +(sumvac[j][t+1]-V[j][t+1]);
				sumvac2ndD[j][t+1]= sumvac2ndD[j][t] + dt/2*(vac2ndDose[j][t]+vac2ndDose[j][t+1]);
			}
			
			if(t>14/dt){
				CI14[t]=(CC[t]-CC[(int)(t-(14/dt))])*100000/totalpop; 
				if((t>=lambda[currentncm])&&(adaptative==true)){
					if((CI14[t]<threshold[0])&&(csvm[currentncm+1]!=thm[0])){
						currentncm++;
						lambda  [currentncm]   = t+1;
						csvm    [currentncm+1] = thm[0];
						csvkappa[currentncm+1] = thkappa[0];
					}
					else if((CI14[t]>=threshold[1])&&(csvm[currentncm+1]!=thm[1])){
						currentncm++;
						lambda  [currentncm]   = t+1;
						csvm    [currentncm+1] = thm[1];
						csvkappa[currentncm+1] = thkappa[1];
					}
					cmeasures = evaluateControlMeasures(cmeasures, t+1.5, csvkappa, csvm, kappasanit, msanit);
					cmeasures = evaluateControlMeasures(cmeasures, t+2, csvkappa, csvm, kappasanit, msanit);
				}
			}

			//---------------------------------------------------------------------------------------------------
			
		}
		
		double[][] estdata = {CC, CD, H, Q};
		
		int minTmax = Math.min(NhistC, dmax);
		
		for(int j=0;j<minTmax;j++){
			for (int k=0; k<numerrors-1; k++){
				difdata [k][j] = (repdata[firstIndex+j][k]-estdata[k][tstepc[j]])*(repdata[firstIndex+j][k]-estdata[k][tstepc[j]]);
				difpenal[k][j] = (Math.max(repdata[firstIndex+j][k]-estdata[k][tstepc[j]], 0.0))*(Math.max(repdata[firstIndex+j][k]-estdata[k][tstepc[j]], 0.0));
			}
		}
		int j = minTmax-1;
		for (int k=0; k<numerrors-1; k++){		
			normdif[k]      = trapz(timec,difdata [k]);
			normdifpenal[k] = trapz(timec,difpenal[k]);
			ndifpenlast     = (Math.max(repdata[firstIndex+j][k]-estdata[k][tstepc[j]], 0.0))*(Math.max(repdata[firstIndex+j][k]-estdata[k][tstepc[j]], 0.0));
			relerror[k]= (normdif[k]+rho[0]*normdifpenal[k])/normRepData[k]+rho[1]*ndifpenlast/(repdata[firstIndex+j][k]*repdata[firstIndex+j][k]);
		}
		
		double[] difdataund  = new double[tund.length];
		double[] difpenalund = new double[tund.length];
		for(int ju=0;ju<tund.length;ju++){
			difdataund[ju] = (repUnD[firstIndex+ju]-Du[tund[ju]])*(repUnD[firstIndex+ju]-Du[tund[ju]]);
			difpenalund[ju] = (Math.max(repUnD[firstIndex+ju]-Du[tund[ju]], 0.0))*(Math.max(repUnD[firstIndex+ju]-Du[tund[ju]], 0.0));
		}
		int ju = tund.length-1;
		ndifpenlast     = (Math.max(repUnD[firstIndex+ju]-Du[tund[ju]], 0.0))*(Math.max(repUnD[firstIndex+ju]-Du[tund[ju]], 0.0));
		relerror[numerrors-1]= (trapz(timecund,difdataund)+rho[0]*trapz(timecund,difpenalund))/normRepDataUnD+rho[1]*ndifpenlast/(repUnD[firstIndex+ju]*repUnD[firstIndex+ju]);
		
		return relerror;
	}
	
	public double[][] evaluateControlMeasures(double[][] cmeas, double tstep, double[] kappasocial, double[] msocial, double[] kappasanit, double[] msanit){
		double socialcm = 1.0;
		double sanitcm  = 1.0;
		
		int k = ncm-1;
		
		for(int i=1;i<ncm-1; i++){
			if (tstep<=lambda[i]){
				k = i;
				break;
			}
		}
		
		if (k==1){
			socialcm = (msocial[0]-msocial[1])*Math.exp(-kappasocial[1]*(tstep-lambda[0])*dt)+msocial[1];
			sanitcm  = (msanit [0]-msanit [1])*Math.exp(-kappasanit [1]*(tstep-lambda[0])*dt)+msanit [1];
		}
		else {
			socialcm = (cmeas[(int)(2*lambda[k-1])][0]-msocial[k])*Math.exp(-kappasocial[k]*(tstep-lambda[k-1])*dt)+msocial[k];
			sanitcm  = (cmeas[(int)(2*lambda[1])][1]-msanit [2])*Math.exp(-kappasanit [2]*(tstep-lambda[1])*dt)+msanit [2];
		}
		
		cmeas[(int) (2*tstep)][0] = socialcm; 
		cmeas[(int) (2*tstep)][1] = sanitcm;
		
		return cmeas;
	}
	
	public double[] evaluateGamma(double tstep, double[] dur, double sanitcm, double varduri){
		double gt = varduri * (1-sanitcm);
		
		double[] currentgamma = new double[numstates-1];
		currentgamma[0] = 1/ dur[0];       //gammae
		currentgamma[1] = 1/(dur[1] - gt); //gammai			// 1/duri;      // Transition rate from I to H  
		currentgamma[2] = 1/(dur[2] + gt); //gammaiu		// 1/duriu;
		currentgamma[3] = 1/(dur[3] + gt); //gammahr
		currentgamma[4] = 1/(dur[4] + gt); //gammahd 		// 1/durhd;    // Transition rate from H to D  
		currentgamma[5] = 1/ dur[5];       //gammaq 
		
		int counterch=0;
		// if any gamma has a sudden change 
		for(int g=0; g<currentgamma.length;g++){
			if ((tstepdurch[g]>0)&&(tstep>tstepdurch[g])){
				currentgamma[g]= 1.0/dur[currentgamma.length+counterch];
				counterch++;
			}
			else if((tstepdurchlin[2*g+1]>0)&&(tstep>tstepdurchlin[2*g+1])){
				currentgamma[g]= 1.0/dur[currentgamma.length+counterch];
				counterch++;
			}
			else if((tstepdurchlin[2*g]>0)&&(tstep>tstepdurchlin[2*g])){
				currentgamma[g]= 1.0/(dur[g] + ((dur[currentgamma.length+counterch]-dur[g])/((tstepdurchlin[2*g+1]-tstepdurchlin[2*g])))*(tstep-tstepdurchlin[2*g]));
				counterch++;
			}
			else if((dursuddench[g]==true)||(durlinch[g]==true)){
				counterch++;
			}
		}
		
		return currentgamma;
		
	}
	
	public double[][] evaluateBeta(double tstep, double betai0, double[] coef, double[] cmeas, double[] cgamma, double cfrate, double ctheta, double p, double wu, double ieta){
		
		double[] mbetae = new double[nvariants];
		double[] mbetai = new double[nvariants];
		double[] mbetaiu = new double[nvariants];
		double[] mbetahr = new double[nvariants];
		double[] mbetahd = new double[nvariants];
		
		mbetae[0]  = cmeas[0]*coef[0]*betai0;
		
		mbetai[0]  = cmeas[0]*betai0;
		
		double betainf = coef[1] * betai0;
		double betaiu  = betainf + ((betai0-betainf)/(1-cfrate))*(1-ctheta);
		mbetaiu[0] = cmeas[0]*betaiu;
		
		mbetahr[0] = (ieta*((mbetai[0]/cgamma[1]) + (mbetae[0]/cgamma[0])+ (1-ctheta-wu)*(mbetaiu[0]/cgamma[2])))/((1-ieta)*((p*(ctheta-cfrate)/cgamma[3])+cfrate/cgamma[4]));
		mbetahr[0] = mbetahr[0]*coefbetaH[(int)Math.floor(tstep*dt)];
		mbetahd[0] = mbetahr[0];
		for(int i=1; i<nvariants; i++) {
			mbetae[i] = (1+csvk[i-1])*mbetae[0];
			mbetai[i] = (1+csvk[i-1])*mbetai[0];
			mbetaiu[i]= (1+csvk[i-1])*mbetaiu[0];
			mbetahr[i]= (1+csvk[i-1])*mbetahr[0];
			mbetahd[i]= (1+csvk[i-1])*mbetahd[0];
		}
		
		double[][] currentbeta = {mbetae, mbetai, mbetaiu, mbetahr, mbetahd};
		
		return currentbeta;
	}
	
	public double[] computeEta(int delay){
		double[] eta = new double[NhistC];
		double[] etaprom = new double[NhistC];
		int firstindex = 0;
		double hrold;
		double crold;
		while (repdata[firstindex+delay-1][5]==0){ //repdata[t][5] = datoHW[t];
			firstindex++;
		}
		
		hrold = repdata[firstindex+delay-1][5];
		crold = repdata[firstindex+delay-1][0];
		for (int d=0; d<firstindex; d++){
			eta[d] = hrold/crold;
		}

		int countz = 0;
		for (int d=firstindex; d+delay<NhistC; d++){
			//if ((repdata[d+delay][5]!=0)&&(repdata[d+delay][0]!=crold)){
			if ((repdata[d+delay][5]!=0)&&(repdata[d+delay][0]!=crold)&&(repdata[d+delay][5]-hrold<repdata[d+delay][0]-crold)){
				eta[d] =(repdata[d+delay][5]-hrold)/(repdata[d+delay][0]-crold);
				hrold  = repdata[d+delay][5];
				crold  = repdata[d+delay][0];
				for(int i=countz; i>0; i--){
					eta[d-i] = eta[d-countz-1]+(eta[d]-eta[d-countz-1])*(countz-i+1)/(countz+1);
				}
				countz = 0;
			}
			else{
				countz++;
			}
		}
		for (int d=NhistC-delay-countz; d<NhistC; d++){
			eta[d] = eta[NhistC-delay-1-countz];
		}
		
		for(int i=3; i<NhistC-3;i++){
			etaprom[i] = (eta[i-3] + eta[i-2] + eta[i-1] + eta[i] + eta[i+1] + eta[i+2] + eta[i+3])/7.0;
		}
		etaprom[0]=etaprom[3];
		etaprom[1]=etaprom[3];
		etaprom[2]=etaprom[3];
		etaprom[NhistC-3]=etaprom[NhistC-4];
		etaprom[NhistC-2]=etaprom[NhistC-4];
		etaprom[NhistC-1]=etaprom[NhistC-4];
		
		return etaprom;
	}
	
	public double[] interpLineal(double[] vector){
		int timax = (int) ((dmax-1)/dt)+1;
		double[] vout = new double[2*timax+1];
		
		for (int i=0; i<vout.length; i++){
			if((int) Math.floor(i*dt/2)+1<vector.length){
				vout[i] = vector[(int) Math.floor(i*dt/2)] + (vector[(int) Math.floor(i*dt/2)+1]-vector[(int) Math.floor(i*dt/2)])*(i-((int) Math.floor(i*dt/2))/(dt/2))/(1/(dt/2));
			}
			else{
				vout[i] = vout[i-1];
			}
		}
		
		return vout; 
	}
	
	public double[] computeCFR(){
		double[] icfr = new double[NhistC];
		double[] cfr  = new double[NhistC+2]; // the 2 final positions are to store the firstindex (tiCFR) and the lastindex
		
		int firstindex = 1;
		double drold;
		double crold;
		while ((repdata[firstindex][0]-repdata[firstindex-1][0]==0)||(repdata[(int) (firstindex+sX[4][(int) ((firstindex/dt))])][1]==0)){ 
			firstindex++;
		}
		drold = repdata[(int) (firstindex+sX[4][(int) ((firstindex/dt))])][1];
		crold = repdata[firstindex][0];
		for (int d=0; d<=firstindex; d++){
			icfr[d] = drold/crold;
			cfr [d] = icfr[d];
		}

		int countz = 0;
		for (int d=firstindex+1; d+sX[4][(int) ((d/dt))]<NhistC; d++){
			if ((repdata[d][0]!=crold)){//>
				icfr[d] =(repdata[(int) (d+sX[4][(int) ((d/dt))])][1]-drold)/(repdata[d][0]-crold);
				drold   = repdata[(int) (d+sX[4][(int) ((d/dt))])][1];
				crold   = repdata[d][0];
				for(int i=countz; i>0; i--){
					icfr[d-i] = icfr[d-countz-1]+(icfr[d]-icfr[d-countz-1])*(countz-i+1)/(countz+1);
				}
				countz = 0;
			}
			else{
				countz++;
			}
		}
		
		for (int d=firstindex+1; d+sX[4][(int) ((d/dt))]<NhistC; d++){
			if(d<6){
				cfr [d] = cfr[0];
			}
			else{
				cfr [d] = (icfr[d]+icfr[d-1]+icfr[d-2]+icfr[d-3]+icfr[d-4]+icfr[d-5]+icfr[d-6]);
				int ibef = 7;
				while ((cfr[d]/ibef<0.01)&&(d>=ibef)){
					cfr[d] = cfr[d] + icfr[d-ibef];
					ibef++;
				}
				cfr[d] = cfr[d]/ibef;
			}
			cfr[NhistC+1] = d;
		}
		
		//the 2 final positions are to store the firstindex (tiCFR) and the lastindex
		cfr[NhistC] = firstindex;
		
		return cfr;
	}
	
	public double evaluateTheta(int tfor, double tstep, double[] fr, double[] cfr){
		double ctheta; 
		
		double inith = Math.max(cfr[NhistC]+6, lambda[1]*dt);

		if(tstep<=inith/dt){     // tthetaini = cfr[NhistC]+6 
			ctheta = fr[2*(int) (Math.floor(inith/dt))]/cfr[(int)(inith)];
		}
		else if (tstep<=cfr[NhistC+1]/dt){ // tthetafin = cfr[NhistC+1]
			ctheta = fr[2*(int) (Math.floor((tfor)*dt)/dt)]/cfr[(int) Math.floor((tfor)*dt)] + (fr[2*(int) ((Math.floor((tfor)*dt)+1)/dt)]/cfr[(int) Math.floor((tfor)*dt)+1]-fr[2*(int) (Math.floor((tfor)*dt)/dt)]/cfr[(int) Math.floor((tfor)*dt)])*(tstep-((int) Math.floor((tfor)*dt))/dt)/(1/(dt)); 
		}
		else{
			ctheta = fr[2*(int) (Math.floor((cfr[NhistC+1])/dt))]/cfr[(int)(cfr[NhistC+1])];
		}
		
		ctheta = Math.min(ctheta, 1.0);
		
		return ctheta;
	}
	
	public double evaluateP(double pini, double thetaini, double thetat, double omegaini, double omegat){
		double p;
		if(thetat-omegat>=thetaini-omegaini){
			p = pini*(thetaini-omegaini)/(thetat-omegat);
		}
		else{
			p = 1-(((1-pini)/(thetaini-omegaini))*(thetat-omegat));
		}
		return p;
	}
	
	public double evaluateOmegaU(double tstep, double wuadj){
		double wu;
		if(tstep<=tund[tund.length-1]-1/dt){
			wu = wuadj;
		}
		else if(tstep<=tund[tund.length-1]){
			wu = -wuadj*(tstep-tund[tund.length-1])/(1/dt);
		}
		else{
			wu = 0.0;
		}
		return wu;
	}
	
	public double[][] systemf(double tstep, double[][] SEIIuHRHDQ, double[][] beta, double[] gamma, double[] fatrate, double[] theta, double[] p, double omegau, double[] vjRK){
		double[] S = SEIIuHRHDQ[0];
		double[] E = SEIIuHRHDQ[1];
		double[] I = SEIIuHRHDQ[2];
		double[] Iu= SEIIuHRHDQ[3];
		double[] HR= SEIIuHRHDQ[4];
		double[] HD= SEIIuHRHDQ[5];
		double[] Q = SEIIuHRHDQ[6];
		
		double[] mbetae = beta[0];
		double[] mbetai = beta[1];
		double[] mbetaiu= beta[2];
		double[] mbetahr= beta[3];
		double[] mbetahd= beta[4];
		
		double gammae = gamma[0];
		double gammai = gamma[1];
		double gammaiu= gamma[2];
		double gammahr= gamma[3];
		double gammahd= gamma[4];
		double gammaq = gamma[5];
		
		// Flujos new
		double[] fS = new double[nvariants];
		double[] fE = new double[nvariants];
		double[] fI = new double[nvariants];
		double[] fIu = new double[nvariants];
		double[] fHR = new double[nvariants];
		double[] fHD = new double[nvariants];
		double[] fQ = new double[nvariants];
		double neweS = 0.0;
		for (int i=0; i<nvariants; i++){
			double newe = S[0]*(mbetae[i]*E[i] + mbetai[i]*I[i] + mbetaiu[i]*Iu[i] + mbetahr[i]*HR[i] + mbetahd[i]*HD[i])/totalpop;
			double newi = gammae * E[i]; 
			double newhid = gammai  * I[i];
			double newhiu = gammaiu * Iu[i];
			double newr = gammahr * HR[i];
			double newd = gammahd * HD[i]; 
			double newq = gammaq  * Q[i];
			
			neweS = neweS + newe;
			fE[i] = newe-newi;
			fI[i] = newi - newhid; 
			fIu[i]= (1-theta[i]-omegau)*newhid - newhiu; 
			fHR[i]= p[i]*(theta[i]-fatrate[i])*newhid - newr;
			fHD[i]= fatrate[i] *newhid - newd ;
			fQ[i] = (1-p[i])*(theta[i]-fatrate[i])*newhid + newr - newq;
		}
        
		fS[0] =  - neweS -(vjRK[(int) Math.floor(tstep)]); 
		
		double[][] fSEIIuHRHDQ = {fS, fE, fI, fIu, fHR, fHD, fQ};
		
		return fSEIIuHRHDQ;
	}
	
	private double trapz(double[] x, double[] y){
		double integral = 0.0;
		for (int n=0; n<x.length-1; n++){
			integral = integral + (x[n+1]-x[n])*(y[n]+y[n+1]);
		}
		integral = integral/2.0;
		return integral;
	}
	
	// TO EVALUATE WITH POSTPROCESSING:
	public void evaluatepost (int nfolder, double delay, double[] dur, double varduri, double betai, double[] coef, double[] kappa, double[] m, double[] kappasanit, double[] msanit, double[] omega, double pcte) {
		
		double [] relerror = new double[numerrors] ; // Relative error committed in each time series
		
		// Matrix initialization --------------------------------------------------------------------------
		int tmax = (int) ((dmax-1)/dt)+1; // Number of model time steps
		int thistC = (int) ((NhistC-1)/dt)+1; // Number of model time steps (reported data)
		int thistmax= Math.max(tmax, thistC);
		int delayini=(int)(delay/dt);
		double[][][] states  = new double[tmax][numstates][nvariants]; // initialize states matrix
		//states = {S[t],E[t],I[t],Iu[t],HR[t], HD[t], Q[t]};
		double[] H  = new double[tmax]; // initialize Hospitalized matrix (H=HR+HD)
		double[] Q  = new double[tmax]; // initialize Quarantine matrix (Q = states[:][numstates-1])
		double[] R  = new double[tmax]; // initialize Recovered matrix
		double[] D  = new double[tmax]; // initialize Death matrix
		double[] Du = new double[tmax]; // initialize Death Undetected matrix
		double[] Ru = new double[tmax]; // initialize Recovered Undetected matrix
		double[] Iu = new double[tmax]; // initialize Infectious Undetected matrix
		double[] CC = new double[tmax]; // initialize Cumulative Cases matrix
		double[] CD = new double[tmax]; // initialize Cumulative Deaths matrix
		double[][] V= new double[nvaccines][tmax]; // initialize Immune Vaccinated matrix 
		double[] CI14=new double[tmax]; // 14-days Cumulative Incidence 
		//-----------------------------------------------------------------------------------------------
		
		// Matrix initialization for PARAMETERS DEPENDING ON TIME ----------------------------------------
		// multiplicamos por dos el tiempo para poder hacer RK4 en t+0.5
		double[][][] beta   = new double[2*tmax+1][numstates-2][nvariants];  // beta = {mbetae, mbetai, mbetaiu, mbetahr, mbetahd}
		double[][] gamma    = new double[2*thistmax+1][numstates-1]; // gamma= {gammae, gammai, gammaiu, gammahr, gammahd, gammaq}
		double[][] cmeasures= new double[2*thistmax+1][2]; // cmeasures = {SOCIAL control measures mc(t), SANITARY control measures mcsanit(t)}
		double[][] frate 	= new double[2*thistmax+1][nvariants]; // frate=OMEGA (fatality rate)
		double[][] theta    = new double[2*tmax+1][nvariants];
		double[][] p        = new double[2*tmax+1][nvariants];
		double[] omegau  = new double[2*tmax+1];//omega[2];
		//-----------------------------------------------------------------------------------------------
		
		// To compare with reported data-----------------------------------------------------------------
		double[][] difdata   = new double[numerrors-1][NhistC];
		double[][] difpenal  = new double[numerrors-1][NhistC];
		double[] normdif     = new double[numerrors-1];
		double[] normdifpenal= new double[numerrors-1];
		double ndifpenlast   = 0.0;
		// ----------------------------------------------------------------------------------------------
		
		// STATES INITIALIZATION -------------------------------------------------------------------------------
		double sumE = 0.0;
		for(int i=0;i<nvariants;i++){
			states[delayini][1][i] = repdata[(int) Math.floor(delayini*dt)][6+i];//repdata[t][6] = datoImpE[t];
			sumE = sumE + states[delayini][1][i];
		}
		states[delayini][0][0] = totalpop - sumE;
		//----------------------------------------------------------------------------------------------------------
		double[][] vaccinated = new double[nvaccines][thistmax];
		double[][] vj         = new double[nvaccines][thistmax];
		double[]   vjsum      = new double[thistmax];
		double[]   difvac     = new double[thistmax];
		double[][] sumvac     = new double[nvaccines][thistmax];
		double[]   Fvac		  = new double[thistmax];
		double[]   ASvac      = new double[thistmax];
		double[][] intAS      = new double[nvaccines][thistmax];
		double[][] vac2ndDose = new double[nvaccines][thistmax];
		double[][] sumvac2ndD = new double[nvaccines][thistmax];
		// Vaccinated
		for (int j=0; j<nvaccines; j++){
			if(numseries-1>8+nvariants+j){
				vaccinated[j][delayini] = repdata[(int) Math.floor(delayini*dt)][8+nvariants+j];
			}
			else if(dailyvaccestimation==false){ 
				for(int i=0;i<nvacweek[j].length;i++){
					if((int) Math.floor(delayini*dt)>=initvacc[j]+7*i){
						vaccinated[j][delayini] = nvacweek[j][i]/7.0;
					}
				}
			}
			else{
				vaccinated[j][delayini] = nvacday[j][(int) Math.floor(delayini*dt)];
			}
		}
		
		// POSTPROCESSING -------------------------------------------------------------------------------
		double[][] CCtotal  = new double[tmax][nvariants];
		double[][] CCu      = new double[tmax][nvariants];
		double[][] causeE   = new double[tmax][nvariants];
		double[][] causeI   = new double[tmax][nvariants]; // initialize causei matrix
		double[][] causeIu  = new double[tmax][nvariants]; // initialize causeiu matrix
		double[][] causeH   = new double[tmax][nvariants]; // initialize causeh matrix
		double[][] CHos     = new double[tmax][nvariants];
		double[][] CCuei    = new double[tmax][nvariants];
		double[][] CCtuei   = new double[tmax][nvariants];
		double[][] altasHos = new double[tmax][nvariants];
		double[][] newE     = new double[tmax][nvariants];
		double[][] newH     = new double[tmax][nvariants];
		double[][] newCases = new double[tmax][nvariants];
		double[][] newDeaths= new double[tmax][nvariants];
		double[][] betainE  = new double[tmax][nvariants];
		double[][] betainI  = new double[tmax][nvariants];
		double[][] betainIu = new double[tmax][nvariants];
		double[][] betainHR = new double[tmax][nvariants];
		double[][] betainHD = new double[tmax][nvariants];
		double[][] S = new double[tmax][nvariants];
		double[][] E = new double[tmax][nvariants];
		double[][] I = new double[tmax][nvariants];
		double[][] Iuv=new double[tmax][nvariants];
		double[][] HR= new double[tmax][nvariants];
		double[][] HD= new double[tmax][nvariants];
		double[][] Qv= new double[tmax][nvariants]; // Q separated for the different variants
		double[][] Rv= new double[tmax][nvariants];
		double[][] Dv= new double[tmax][nvariants];
		double[][] Duv=new double[tmax][nvariants];
		double[][] CCv=new double[tmax][nvariants];
		double[][] CDv=new double[tmax][nvariants];
		S[delayini] = states[delayini][0]; 
		E[delayini] = states[delayini][1]; 
		I[delayini] = states[delayini][2];
		Iuv[delayini]=states[delayini][3]; 
		HR[delayini]= states[delayini][4];
		HD[delayini]= states[delayini][5];
		Qv[delayini]= states[delayini][6]; 
		int timestop = tmax;
		//-----------------------------------------------------------------------------------------------
		
		// STRATEGY TO COMPUTE THETA:
		for (double t=delayini; t<thistmax+0.5; t=t+0.5){
			cmeasures = evaluateControlMeasures(cmeasures, t, kappa, m, kappasanit, msanit);
			gamma[(int)(2*t)] = evaluateGamma(t, dur, cmeasures[(int)(2*t)][1], varduri);
			frate[(int)(2*t)][0] = cmeasures[(int)(2*t)][1]*omega[1] + (1-cmeasures[(int)(2*t)][1])*omega[0];
			frate[(int)(2*t)][0] = frate[(int)(2*t)][0]*coefmort[(int)Math.floor(t*dt)];
			for(int i=1; i<nvariants; i++){
				frate[(int)(2*t)][i] = (1+csvfr[i-1])*frate[(int)(2*t)][0];
			}
		}
		double[] repfrateef = computeCFR();//(gamma);
		//----------------------------------------------------------------------------------------------------------
		
		double[] etat = interpLineal(computeEta((int)Math.round(1/gamma[0][0]+1/gamma[0][1])));
		
		// CALCULO PARAMETROS EN t inicial (se guardan en la posición 2*delayini)-----------------------------------
		int tthetai = (int) ((repfrateef[NhistC]+6)/dt);// tthetaini = cfr[NhistC]+6
		double evaltheta = evaluateTheta(delayini, delayini, getColumn(frate,0), repfrateef);
		double coeftheta = propvar[(int)(Math.floor(delayini*dt))][0];
		for(int i=1; i<nvariants; i++){
			coeftheta = coeftheta + (1+csvfr[i-1])*propvar[(int)(Math.floor(delayini*dt))][i];
		}
		for(int i=0; i<nvariants; i++){
			theta[2*delayini][i] = Math.min(1.0,coeftheta*evaltheta);
			p    [2*delayini][i] = evaluateP(pcte, theta[2*delayini][i], theta[2*delayini][i], frate[2*tthetai][i], frate[2*delayini][i]);
		}
		omegau[2*delayini]= evaluateOmegaU(delayini, omega[2]);
		beta  [2*delayini]= evaluateBeta  (delayini, betai, coef, cmeasures[2*delayini], gamma[2*delayini], frate[2*delayini][0], theta[2*delayini][0], p[2*delayini][0], omegau[2*delayini], etat[2*delayini]);
		//----------------------------------------------------------------------------------------------------------
		
		for(int t=delayini; t<tmax-1; t++){
			
			// CALCULO PARAMETROS EN t+0.5 (se guardan en la posición 2*t+1)------------------------------------------
			evaltheta = evaluateTheta(t, t+0.5, getColumn(frate,0), repfrateef);
			coeftheta = propvar[(int)(Math.floor((t+0.5)*dt))][0];
			for(int i=1; i<nvariants; i++){
				coeftheta = coeftheta + (1+csvfr[i-1])*propvar[(int)(Math.floor((t+0.5)*dt))][i];
			}
			for(int i=0; i<nvariants; i++){
				theta[2*t+1][i] = Math.min(1.0,coeftheta*evaltheta); 
				p    [2*t+1][i] = evaluateP(pcte, theta[2*delayini][i], theta[2*t+1][i], frate[2*tthetai][i], frate[2*t+1][i]);
			}
			omegau[2*t+1]= evaluateOmegaU(t+0.5, omega[2]);
			beta  [2*t+1]= evaluateBeta  (t+0.5, betai, coef, cmeasures[2*t+1], gamma[2*t+1], frate[2*t+1][0], theta[2*t+1][0], p[2*t+1][0], omegau[2*t+1], etat[2*t+1]);
			//----------------------------------------------------------------------------------------------------------
			
			// CALCULO PARAMETROS EN t+1 (se guardan en la posición 2*(t+1))------------------------------------------
			evaltheta = evaluateTheta(t, t+1, getColumn(frate,0), repfrateef);
			coeftheta = propvar[(int)(Math.floor((t+1)*dt))][0];
			for(int i=1; i<nvariants; i++){
				coeftheta = coeftheta + (1+csvfr[i-1])*propvar[(int)(Math.floor((t+1)*dt))][i];
			}
			for(int i=0; i<nvariants; i++){
				theta[2*t+2][i] = Math.min(1.0,coeftheta*evaltheta); 
				p    [2*t+2][i] = evaluateP(pcte, theta[2*delayini][i], theta[2*t+2][i], frate[2*tthetai][i], frate[2*t+2][i]);
			}
			omegau[2*t+2]= evaluateOmegaU(t+1, omega[2]);
			beta  [2*t+2]= evaluateBeta  (t+1, betai, coef, cmeasures[2*t+2], gamma[2*t+2], frate[2*t+2][0], theta[2*t+2][0], p[2*t+2][0], omegau[2*t+2], etat[2*t+2]);
			
			for (int j=0; j<nvaccines; j++){
				for(int k=1; k<efficacy[j].length;k++){
					if((((int) Math.floor((t+1)*dt))>=daysefficacy[j][k])&&(efficacy[j][k]>0)){
						vj[j][t+1] = vj[j][t+1] + (efficacy[j][k]-efficacy[j][k-1])*vaccinated[j][(int) ((t+1)-(daysefficacy[j][k]/dt))]*(ASvac[(int) ((t+1)-(daysefficacy[j][k]/dt))]/Fvac[(int) ((t+1)-(daysefficacy[j][k]/dt))]);
					}
				}
				vjsum[t+1] = vjsum[t+1] + vj[j][t+1];
			}
			
			//----------------------------------------------------------------------------------------------------------
			double[][] fsyseval = systemf(t, states[t], beta[2*t], gamma[2*t], frate[2*t], theta[2*t], p[2*t], omegau[2*t], vjsum);
			
			// RUNGE KUTTA 4 etapas y orden 4 
			double[][] statesF2 = new double [numstates][nvariants];
			
			for (int s=0; s<numstates; s++){
				for (int i=0; i<nvariants; i++){
					statesF2[s][i] = states[t][s][i] + dt/2*fsyseval[s][i];
				}
			}

			double[][] fsysevalF2 = systemf(t+0.5, statesF2, beta[2*t+1], gamma[2*t+1], frate[2*t+1], theta[2*t+1], p[2*t+1], omegau[2*t+1], vjsum);
			
			double[][] statesF3 = new double [numstates][nvariants];
			for (int s=0; s<numstates; s++){
				for (int i=0; i<nvariants; i++){
					statesF3[s][i] = states[t][s][i] + dt/2*fsysevalF2[s][i];
				}
			}
			  
			double[][] fsysevalF3 = systemf(t+0.5, statesF3, beta[2*t+1], gamma[2*t+1], frate[2*t+1], theta[2*t+1], p[2*t+1], omegau[2*t+1], vjsum);
			
			double[][] statesF4 = new double [numstates][nvariants];
			for (int s=0; s<numstates; s++){
				for (int i=0; i<nvariants; i++){
					statesF4[s][i] = states[t][s][i] + dt*fsysevalF3[s][i];
				}
			}
			
			double[][] fsysevalF4 = systemf(t+1, statesF4, beta[2*t+2], gamma[2*t+2], frate[2*t+2], theta[2*t+2], p[2*t+2], omegau[2*t+2], vjsum);
			
			for (int s=0; s<numstates; s++){
				for (int i=0; i<nvariants; i++){
					states[t+1][s][i] = states[t][s][i] + dt/6*(fsyseval[s][i]+2*fsysevalF2[s][i]+2*fsysevalF3[s][i]+fsysevalF4[s][i]);
				}
			}
			
			D[t+1] = D[t];
			Du[t+1] = Du[t];
			CC[t+1] = CC[t];
			for (int i=0; i<nvariants; i++) {
				H[t+1] = H[t+1] + states[t+1][4][i] + states[t+1][5][i]; // 4 is HR and 5 is HD
				Q[t+1] = Q[t+1] + states[t+1][6][i]; // 6 is Q
				Iu[t+1]= Iu[t+1]+ states[t+1][3][i]; // 3 is Iu
				D[t+1] = D[t+1] + dt/2*(gamma[2*t][4]*states[t][5][i]+gamma[2*(t+1)][4]*states[t+1][5][i]); // HD = states[5]; gammahd = gamma[4]
				Du[t+1]= Du[t+1]+ dt/2*(omegau[2*t]*gamma[2*t][1]*states[t][2][i]+omegau[2*(t+1)]*gamma[2*(t+1)][1]*states[t+1][2][i]); 
				CC[t+1]= CC[t+1]+ dt/2*(theta[2*t][i]*gamma[2*t][1]*states[t][2][i]+theta[2*(t+1)][i]*gamma[2*(t+1)][1]*states[t+1][2][i]); // I= states[2]; gammai = gamma[1]
			}
			R [t+1]= R[t] + dt/2*(gamma[2*t][5]*Q[t] + gamma[2*(t+1)][5]*Q[t+1]);  // gammaq=gamma[5]
			Ru[t+1]= Ru[t]+ dt/2*(gamma[2*t][2]*Iu[t]+ gamma[2*(t+1)][2]*Iu[t+1]); // gammaIu=gamma[2]
			CD[t+1]= D[t+1];
			
			
			if(((t+1)%(1/dt)==0)&&((t+1)*dt<NhistC)) {
				for(int i=0; i<nvariants; i++){
					states[t+1][1][i]=states[t+1][1][i]+repdata[(int) Math.floor((t+1)*dt)][6+i]; //repdata[t][6] = datoImpE[t];
				}
				states[t+1][4][0]=states[t+1][4][0]-repdata[(int) Math.floor((t+1)*dt)][6+nvariants]; //repdata[t][7] = datoEvac[t];
			}
			
			// Vaccinated
			double auxsumEi=0.0;
			ASvac [t] = states[t][0][0]; 
			for (int j=0; j<nvaccines; j++){
				for(int i=0; i<nvariants; i++){ 
					auxsumEi= auxsumEi + states[t][1][i]; 
				}
				Fvac  [t]     = states[t][0][0]+auxsumEi+R[Math.max((int) (t-tRdvacdays/dt),0)]+Ru[t]- difvac[t];
				ASvac [t]     = ASvac [t] -(intAS[j][Math.max(t-1, 0)]-V[j][t]);
				intAS [j][t]  = intAS [j][Math.max(t-1, 0)] + dt*(ASvac[Math.max(t-1, 0)]*vaccinated[j][Math.max(t-1, 0)]/Fvac[Math.max(t-1, 0)]);
			}
			
			if((ASvac[t]<0)||(Fvac[t]<=0)){
				ASvac [t]= 0.0;
				difvac[t]= 0.0;
				for (int j=0;j<nvaccines;j++){
					vaccinated[j][t] = 0.0;
					sumvac    [j][t] = sumvac[j][Math.max(t-1, 0)] + dt/2*(vaccinated[j][Math.max(t-1, 0)]+vaccinated[j][t]);
					difvac       [t] = difvac[t]  +(sumvac[j][t]-V[j][t]);
				}
			}
			
			if(((t+1)*dt<NhistC)) {
				for (int j=0;j<nvaccines;j++){
					if(numseries-1>8+nvariants+j){
						vaccinated[j][t+1] = repdata[(int) Math.floor((t+1)*dt)][8+nvariants+j];
						vac2ndDose[j][t+1] = vaccinated[j][Math.max((int) ((t+1)-days2nd[j]/dt),0)];
					}
					else if(dailyvaccestimation==false){ 
						for(int i=0;i<nvacweek[j].length;i++){
							if((int) Math.floor((t+1)*dt)>=initvacc[j]+7*i){
								if((int) Math.floor((t+1)*dt)<initvacc[j]+days2nd[j]){ 
									vaccinated[j][t+1] = nvacweek[j][i]/7.0;
								}
								else if((i<2)||(nvacweek[j][i]>=nvacweek[j][i-2])||((int) Math.floor((t+1)*dt)>=initvacc[j]+7*nvacweek[j].length)){
									vaccinated[j][t+1] = Math.max((nvacweek[j][i]/7.0)-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
								}
								else{
									vaccinated[j][t+1] = Math.max((nvacweek[j][i]/7.0)-(vaccinated[j][(int) ((t+1)-(days2nd[j]-14)/dt)]-(nvacweek[j][i]/7.0))-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
								}
							}
						}
					}
					else{
						if((int) Math.floor((t+1)*dt)<initvacc[j]+days2nd[j]){ 
							vaccinated[j][t+1] = nvacday[j][(int) Math.floor((t+1)*dt)];
						}
						else{
							vaccinated[j][t+1] = Math.max(nvacday[j][(int) Math.floor((t+1)*dt)]-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
							vac2ndDose[j][t+1] = vaccinated[j][(int) ((t+1)-days2nd[j]/dt)];
						}
					}
				}
			}
			else {
				if(dailyvaccestimation==false){
				for (int j=0; j<nvaccines; j++){
					for(int i=0;i<nvacweek[j].length;i++){
						if((int) Math.floor((t+1)*dt)>=initvacc[j]+7*i){
							if((int) Math.floor((t+1)*dt)<initvacc[j]+days2nd[j]){
								vaccinated[j][t+1] = nvacweek[j][i]/7.0;
							}
							else if((i<2)||(nvacweek[j][i]>=nvacweek[j][i-2])||((int) Math.floor((t)*dt)>=initvacc[j]+7*nvacweek[j].length)){
								vaccinated[j][t+1] = Math.max((nvacweek[j][i]/7.0)-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
							}
							else{
								vaccinated[j][t+1] = Math.max((nvacweek[j][i]/7.0)-(vaccinated[j][(int) ((t+1)-(days2nd[j]-14)/dt)]-(nvacweek[j][i]/7.0))-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
							}
						}
					}
				}
				}
				else{
					for (int j=0; j<nvaccines; j++){
						if((int) Math.floor((t+1)*dt)<initvacc[j]+days2nd[j]){
							vaccinated[j][t+1] = nvacday[j][(int) Math.floor((t+1)*dt)];
						}
						else{
							vaccinated[j][t+1] = Math.max(nvacday[j][(int) Math.floor((t+1)*dt)]-vaccinated[j][(int) ((t+1)-days2nd[j]/dt)],0.0);
							vac2ndDose[j][t+1] = vaccinated[j][(int) ((t+1)-days2nd[j]/dt)];
						}
					}
				}
			}
			
			for (int j=0; j<nvaccines; j++){
				V     [j][t+1]= V  [j][t]    + dt/2*(vj[j][t] + vj[j][t+1]);
				sumvac[j][t+1]= sumvac[j][t] + dt/2*(vaccinated[j][t]+vaccinated[j][t+1]);
				difvac   [t+1]= difvac[t+1]  +(sumvac[j][t+1]-V[j][t+1]);
				sumvac2ndD[j][t+1]= sumvac2ndD[j][t] + dt/2*(vac2ndDose[j][t]+vac2ndDose[j][t+1]);
			}
			
			if(t>14/dt){
				CI14[t]=(CC[t]-CC[(int)(t-(14/dt))])*100000/totalpop;
				//System.out.println(CI14[t]); 
				if((t>=lambda[currentncm])&&(adaptative==true)){
					if((CI14[t]<threshold[0])&&(csvm[currentncm+1]!=thm[0])){
						currentncm++;
						lambda  [currentncm]   = t+1;
						csvm    [currentncm+1] = thm[0];
						csvkappa[currentncm+1] = thkappa[0];
					}
					else if((CI14[t]>=threshold[1])&&(csvm[currentncm+1]!=thm[1])){
						currentncm++;
						lambda  [currentncm]   = t+1;
						csvm    [currentncm+1] = thm[1];
						csvkappa[currentncm+1] = thkappa[1];
					}
					cmeasures = evaluateControlMeasures(cmeasures, t+1.5, csvkappa, csvm, kappasanit, msanit);
					cmeasures = evaluateControlMeasures(cmeasures, t+2, csvkappa, csvm, kappasanit, msanit);
				}
			}
			//---------------------------------------------------------------------------------------------------
			
			// POSTPROCESSING -----------------------------------------------------------------------------------
			S[t+1] = states[t+1][0];
			E[t+1] = states[t+1][1];
			I[t+1] = states[t+1][2];
			Iuv[t+1]=states[t+1][3];
			HR[t+1]= states[t+1][4];
			HD[t+1]= states[t+1][5];
			Qv[t+1]= states[t+1][6]; // Q separated for the different variants
			
			
			for (int i=0; i<nvariants; i++){ 
				Rv [t+1][i] = Rv [t][i] + dt/2*(gamma[2*t][5]*Qv[t][i] + gamma[2*(t+1)][5]*Qv[t+1][i]);  // gammaq=gamma[5]
				CCv[t+1][i] = CCv[t][i] + dt/2*(theta[2*t][i]*gamma[2*t][1]*states[t][2][i]+theta[2*(t+1)][i]*gamma[2*(t+1)][1]*states[t+1][2][i]); // I= states[2]; gammai = gamma[1]
				Dv [t+1][i] = Dv [t][i] + dt/2*(gamma[2*t][4]*states[t][5][i]+gamma[2*(t+1)][4]*states[t+1][5][i]); // HD = states[5]; gammahd = gamma[4]
				CDv[t+1][i] = Dv[t+1][i];
				Duv[t+1][i] = Duv[t][i]+ dt/2*(omegau[2*t]*gamma[2*t][1]*states[t][2][i]+omegau[2*(t+1)]*gamma[2*(t+1)][1]*states[t+1][2][i]); 
				
				CCtotal[t+1][i] = CCtotal[t][i] + dt/2*(gamma[2*t][1]*I[t][i]+gamma[2*(t+1)][1]*I[t+1][i]);
				causeE [t+1][i] = causeE [t][i] + dt/2*(beta[2*t][0][i]*E[t][i]*S[t][0]+beta[2*(t+1)][0][i]*E[t+1][i]*S[t+1][0])/totalpop;
				causeI [t+1][i] = causeI [t][i] + dt/2*(beta[2*t][1][i]*I[t][i]*S[t][0]+beta[2*(t+1)][1][i]*I[t+1][i]*S[t+1][0])/totalpop;
				causeIu[t+1][i] = causeIu[t][i] + dt/2*(beta[2*t][2][i]*Iuv[t][i]*S[t][0]+beta[2*(t+1)][2][i]*Iuv[t+1][i]*S[t+1][0])/totalpop;
				causeH [t+1][i] = causeH [t][i] + dt/2*(beta[2*t][3][i]*HR[t][i]*S[t][0]+beta[2*t][4][i]*HD[t][i]*S[t][0]+beta[2*(t+1)][3][i]*HR[t+1][i]*S[t+1][0]+beta[2*(t+1)][4][i]*HD[t+1][i]*S[t+1][0])/totalpop;
				altasHos[t+1][i]= altasHos[t][i]+ dt/2*(gamma[2*t][3]*HR[t][i] + gamma[2*(t+1)][3]*HR[t+1][i]);
				CHos   [t+1][i] = CHos   [t][i] + dt/2*((p[2*t][i]*theta[2*t][i]+(1-p[2*t][i])*frate[2*t][i])*gamma[2*t][1]*I[t][i]+(p[2*(t+1)][i]*theta[2*(t+1)][i]+(1-p[2*(t+1)][i])*frate[2*(t+1)][i])*gamma[2*(t+1)][1]*I[t+1][i]);
				CCu    [t+1][i] = CCtotal[t+1][i] - CCv[t+1][i];
				CCuei  [t+1][i] = CCu    [t+1][i] + E[t+1][i]+I[t+1][i];
				CCtuei [t+1][i] = CCtotal[t+1][i] + E[t+1][i]+I[t+1][i];
			}
			
			
			for (int i=0; i<nvariants; i++){
				newCases [t][i] = theta[2*t][i]*gamma[2*t][1]*I[t][i];
				newDeaths[t][i] = gamma[2*t][4]*HD[t][i];
				newH     [t][i] = (p[2*t][i]*(theta[2*t][i]-frate[2*t][i])+frate[2*t][i])*gamma[2*t][1]*I[t][i];
				newE     [t][i] = (beta[2*t][0][i]*E [t][i]+beta[2*t][1][i]*I[t][i]+beta[2*t][2][i]*Iuv[t][i]+beta[2*t][3][i]*HR[t][i]+beta[2*t][4][i]*HD[t][i])*S[t][0]/totalpop;
				betainE  [t][i] = (beta[2*t][0][i]*E [t][i])*S[t][0]/totalpop;
				betainI  [t][i] = (beta[2*t][1][i]*I [t][i])*S[t][0]/totalpop;
				betainIu [t][i] = (beta[2*t][2][i]*Iuv[t][i])*S[t][0]/totalpop;
				betainHR [t][i] = (beta[2*t][3][i]*HR[t][i])*S[t][0]/totalpop;
				betainHD [t][i] = (beta[2*t][4][i]*HD[t][i])*S[t][0]/totalpop;
				
				if(((t)%(1/dt)==0)&&((t)*dt<NhistC)) {
					newE[t][i]  = newE[t][i]+repdata[(int) Math.floor((t)*dt)][6+i];//-repdata[(int) Math.floor((t)*dt)][7]; //repdata[t][7] = datoEvac[t];
				}
			
			}
			if(((t)%(1/dt)==0)&&((t)*dt<NhistC)) {
				newE     [t][0] = newE[t][0] -repdata[(int) Math.floor((t)*dt)][6+nvariants]; //repdata[t][7] = datoEvac[t];
			}
			
			//-----------------------------------------------------------------------------------------------------
			
		}
		
		double[][] estdata = {CC, CD, H, Q};
		
		int minTmax = Math.min(NhistC, dmax);
		
		for(int j=0;j<minTmax;j++){
			for (int k=0; k<numerrors-1; k++){
				difdata [k][j] = (repdata[firstIndex+j][k]-estdata[k][tstepc[j]])*(repdata[firstIndex+j][k]-estdata[k][tstepc[j]]);
				difpenal[k][j] = (Math.max(repdata[firstIndex+j][k]-estdata[k][tstepc[j]], 0.0))*(Math.max(repdata[firstIndex+j][k]-estdata[k][tstepc[j]], 0.0));
			}
		}
		int j = minTmax-1;
		for (int k=0; k<numerrors-1; k++){		
			normdif[k]      = trapz(timec,difdata [k]);
			normdifpenal[k] = trapz(timec,difpenal[k]);
			ndifpenlast     = (Math.max(repdata[firstIndex+j][k]-estdata[k][tstepc[j]], 0.0))*(Math.max(repdata[firstIndex+j][k]-estdata[k][tstepc[j]], 0.0));
			relerror[k]= (normdif[k]+rho[0]*normdifpenal[k])/normRepData[k]+rho[1]*ndifpenlast/(repdata[firstIndex+j][k]*repdata[firstIndex+j][k]);
		}
		
		double[] difdataund  = new double[tund.length];
		double[] difpenalund = new double[tund.length];
		for(int ju=0;ju<tund.length;ju++){
			difdataund[ju] = (repUnD[firstIndex+ju]-Du[tund[ju]])*(repUnD[firstIndex+ju]-Du[tund[ju]]);
			difpenalund[ju] = (Math.max(repUnD[firstIndex+ju]-Du[tund[ju]], 0.0))*(Math.max(repUnD[firstIndex+ju]-Du[tund[ju]], 0.0));
		}
		int ju = tund.length-1;
		ndifpenlast     = (Math.max(repUnD[firstIndex+ju]-Du[tund[ju]], 0.0))*(Math.max(repUnD[firstIndex+ju]-Du[tund[ju]], 0.0));
		relerror[numerrors-1]= (trapz(timecund,difdataund)+rho[0]*trapz(timecund,difpenalund))/normRepDataUnD+rho[1]*ndifpenlast/(repUnD[firstIndex+ju]*repUnD[firstIndex+ju]);

		
		// WRITE RESULTS TO CSV:
		String filePath = outputpath; //+ Integer.toString(nfolder)+"\\";
		File directory = new File(filePath);
	    if (! directory.exists()){
	        directory.mkdirs();
	    }
	    
	    writeStatesCSV(getColumn(S,0),V,E,I,Iuv,HR,HD,Qv,Rv,Dv,Duv,timestop,filePath+"states.csv");
	    writeCumulativeCSV(CCv,CDv,CHos,CCuei,CCtuei,CI14,timestop,filePath+"cumulative.csv"); 
	    writeFeaturesCSV(gamma,getMatrix(beta,0),cmeasures,frate,omegau,theta,p,2*timestop-1,filePath+"features.csv");
	    writeNewInECSV(betainE,betainI,betainIu,betainHR,betainHD,newE,newCases,newH,newDeaths,timestop,filePath+"newin.csv");
	    writeCausesCSV(causeE,causeI,causeIu,causeH,timestop,filePath+"causesOfInfection.csv");
	    if(nvaccines>0){
	    	writeDosesVacCSV(sumvac,sumvac2ndD,timestop,filePath+"dosesOfVaccines.csv");
	    }
	    //String[] namSX={"sX_E","sX_I","sX_Iu","sX_HR","sX_HD"};
	    //writeCSV(namSX, sX, sX[0].length, filePath+"sX.csv");
	    
		computeR(filePath, timestop, getColumn(S,0), newE, beta, frate, theta, p, omegau);
	}
	
	public void evaluatepost(){
		int nfolder  = 1;
		double delay = 0.0;
		double[] kappasanit = {1.0, 1.0, 1.0};
		double[]     msanit = {1.0, 1.0, 0.0};
		double varduri = 0.0; 
		
		evaluatepost (nfolder, delay, csvdur, varduri, csvbetai, csvcoef, csvkappa, csvm, kappasanit, msanit, csvFR, csvp);
	}
	
	// TO WRITE RESULTS TO TXT
	private void writeResult(double[] result, int maxtime, String file){
		FileWriter fichero = null;
        	PrintWriter pw = null;
        	try
        	{
            		fichero = new FileWriter(file);
            		pw = new PrintWriter(fichero);
	
            		for (int i = 0; i < maxtime; i++){
            			pw.println(result[i]);
            		}
        	} catch (Exception e) {
           	 	e.printStackTrace();
        	} finally {
           	try {
           		// Nuevamente aprovechamos el finally para 
           		// asegurarnos que se cierra el fichero.
           		if (null != fichero)
              			fichero.close();
           	} catch (Exception e2) {
             	 	e2.printStackTrace();
           	}
        	}
	}
	
	// TO WRITE RESULTS TO CSV
	private void writeCSV(String[] namescol, double[][] result, int maxtime, String file){
		FileWriter fichero = null;
        PrintWriter pw = null;
        try
        {
            fichero = new FileWriter(file);
            pw = new PrintWriter(fichero);
            // Names of the columns
            pw.print("Date"+SEPARATOR);
            for (int j = 0; j < namescol.length; j++){
        		pw.print(namescol[j]+SEPARATOR);
        	}
            pw.println("");
            // Time series
            for (int i = 0; i < maxtime; i++){
            	Date currentdate = new Date((long)Math.floor(dateinit.getTime()+i*dt*24*3600000));
            	pw.print(format.format(currentdate)+SEPARATOR);
            	for (int j = 0; j < namescol.length; j++){
            		pw.print(result[j][i]+SEPARATOR);
            	}
            	pw.println("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           // Nuevamente aprovechamos el finally para 
           // asegurarnos que se cierra el fichero.
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
	}
	
	private void writeStatesCSV(double[] S, double[][] V, double[][] E, double[][] I, double[][] Iu, double[][] HR, double[][] HD, double[][] Q, double[][] R, double[][] D, double[][] Du, int maxtime, String file){
		String[] basicnames = {"S","V","E","I","Iu","HR","HD","Q","R","D","Du"};
		int len   = basicnames.length;
		String[] namescol   = new String[len+(len-2)*nvariants+(nvaccines-1)];
		double[][] allstates= new double[len+(len-2)*nvariants+(nvaccines-1)][S.length];
		//double[][] allstates= {S,V,E,I,Iu,HR,HD,Q,R,D,Du};
		allstates[0] = S;
		namescol [0] = basicnames[0];
		for(int j=0; j<nvaccines; j++) {
			allstates[j+1] = V[j];
			namescol [j+1] = basicnames[1]+Integer.toString(j+1);
		}
		for(int col=2; col<len; col++){
			namescol[col+(nvaccines-1)] = basicnames[col];
		}
		for(int i=0; i<nvariants;i++){
			for(int t=0; t<S.length;t++){
				allstates[nvaccines+1][t] = allstates[nvaccines+1][t] + getColumn(E, i)[t];
				allstates[nvaccines+2][t] = allstates[nvaccines+2][t] + getColumn(I, i)[t];
				allstates[nvaccines+3][t] = allstates[nvaccines+3][t] + getColumn(Iu,i)[t];
				allstates[nvaccines+4][t] = allstates[nvaccines+4][t] + getColumn(HR,i)[t];
				allstates[nvaccines+5][t] = allstates[nvaccines+5][t] + getColumn(HD,i)[t];
				allstates[nvaccines+6][t] = allstates[nvaccines+6][t] + getColumn(Q, i)[t];
				allstates[nvaccines+7][t] = allstates[nvaccines+7][t] + getColumn(R, i)[t];
				allstates[nvaccines+8][t] = allstates[nvaccines+8][t] + getColumn(D, i)[t];
				allstates[nvaccines+9][t] = allstates[nvaccines+9][t] + getColumn(Du,i)[t];
			}
			allstates[len+(len-2)*i+(nvaccines-1)+0] = getColumn(E, i);
			allstates[len+(len-2)*i+(nvaccines-1)+1] = getColumn(I, i);
			allstates[len+(len-2)*i+(nvaccines-1)+2] = getColumn(Iu,i);
			allstates[len+(len-2)*i+(nvaccines-1)+3] = getColumn(HR,i);
			allstates[len+(len-2)*i+(nvaccines-1)+4] = getColumn(HD,i);
			allstates[len+(len-2)*i+(nvaccines-1)+5] = getColumn(Q, i);
			allstates[len+(len-2)*i+(nvaccines-1)+6] = getColumn(R, i);
			allstates[len+(len-2)*i+(nvaccines-1)+7] = getColumn(D, i);
			allstates[len+(len-2)*i+(nvaccines-1)+8] = getColumn(Du,i);
			namescol [len+(len-2)*i+(nvaccines-1)+0] = basicnames[2]+Integer.toString(i+1);
			namescol [len+(len-2)*i+(nvaccines-1)+1] = basicnames[3]+Integer.toString(i+1);
			namescol [len+(len-2)*i+(nvaccines-1)+2] = basicnames[4]+Integer.toString(i+1);
			namescol [len+(len-2)*i+(nvaccines-1)+3] = basicnames[5]+Integer.toString(i+1);
			namescol [len+(len-2)*i+(nvaccines-1)+4] = basicnames[6]+Integer.toString(i+1);
			namescol [len+(len-2)*i+(nvaccines-1)+5] = basicnames[7]+Integer.toString(i+1);
			namescol [len+(len-2)*i+(nvaccines-1)+6] = basicnames[8]+Integer.toString(i+1);
			namescol [len+(len-2)*i+(nvaccines-1)+7] = basicnames[9]+Integer.toString(i+1);
			namescol [len+(len-2)*i+(nvaccines-1)+8] = basicnames[10]+Integer.toString(i+1);
		}
		writeCSV(namescol, allstates, maxtime, file);
	}
	
	private void writeCumulativeCSV(double[][] cases, double[][] deaths, double[][] chos, double[][] ccuei, double[][] cctotalei, double[] incidence14days, int maxtime, String file){
		String[] basicnames = {"CC", "CD","CHos","CCuEI","CCtotalEI","CI14days"};
		int len = basicnames.length-1;
		String[] namescol   = new String[len*(nvariants+1)+1];
		double[][] results  = new double[len*(nvariants+1)+1][cases.length];
		//double[][] results = {cases,deaths,chos,ccuei,cctotalei,incidence14days};
		for(int col=0; col<len; col++){
			namescol[col] = basicnames[col];
		}
		for(int i=0; i<nvariants;i++){
			for(int t=0; t<cases.length;t++){
				results[0][t] = results[0][t] + getColumn(cases,    i)[t];
				results[1][t] = results[1][t] + getColumn(deaths,   i)[t];
				results[2][t] = results[2][t] + getColumn(chos,     i)[t];
				results[3][t] = results[3][t] + getColumn(ccuei,    i)[t];
				results[4][t] = results[4][t] + getColumn(cctotalei,i)[t];
			}
			results [len*(i+1)+0] = getColumn(cases,     i);
			results [len*(i+1)+1] = getColumn(deaths,    i);
			results [len*(i+1)+2] = getColumn(chos,      i);
			results [len*(i+1)+3] = getColumn(ccuei,     i);
			results [len*(i+1)+4] = getColumn(cctotalei, i);
			namescol[len*(i+1)+0] = basicnames[0]+Integer.toString(i+1);
			namescol[len*(i+1)+1] = basicnames[1]+Integer.toString(i+1);
			namescol[len*(i+1)+2] = basicnames[2]+Integer.toString(i+1);
			namescol[len*(i+1)+3] = basicnames[3]+Integer.toString(i+1);
			namescol[len*(i+1)+4] = basicnames[4]+Integer.toString(i+1);
		}
		namescol[namescol.length-1] = basicnames[basicnames.length-1];
		results [namescol.length-1]=incidence14days;
		writeCSV(namescol, results, maxtime, file);
	}
	
	private void writeFeaturesCSV(double[][] gammas, double[][] betas, double[][] cntrmeas, double[][] omega, double[] omegau, double[][] theta, double[][] p,int maxtime, String file){
		String [] namescol = {"gammae","gammai","gammaiu","gammahr","gammahd","gammaq","mbetae1","mbetai1","mbetaiu1","mbetahr1","mbetahd1","cmsocial","cmsanit","omega","omegau","theta","p"};//,"omega2","theta2","p2"
		FileWriter fichero = null;
        PrintWriter pw = null;
        try
        {
            fichero = new FileWriter(file);
            pw = new PrintWriter(fichero);
            // Names of the columns
            pw.print("Date"+SEPARATOR);
            for (int j = 0; j < namescol.length; j++){
        		pw.print(namescol[j]+SEPARATOR);
        	}
            for(int ivar=1;ivar<nvariants;ivar++){
        		pw.print(namescol[13]+Integer.toString(ivar+1)+SEPARATOR);
        		pw.print(namescol[15]+Integer.toString(ivar+1)+SEPARATOR);
        		pw.print(namescol[16]+Integer.toString(ivar+1)+SEPARATOR);
        	}
            pw.println("");
            // Time series
            for (int i = 0; i < maxtime; i++){
            	Date currentdate = new Date((long)Math.floor(dateinit.getTime()+i*(dt/2)*24*3600000));
            	pw.print(format.format(currentdate)+SEPARATOR);
            	for (int j = 0; j < gammas[i].length; j++){
            		pw.print(gammas[i][j]+SEPARATOR);
            	}
            	for (int j = 0; j < betas[i].length; j++){
            		pw.print(betas[i][j]+SEPARATOR);
            	}
            	for (int j = 0; j < cntrmeas[i].length; j++){
            		pw.print(cntrmeas[i][j]+SEPARATOR);
            	}
            	pw.print(omega [i][0]+SEPARATOR);
            	pw.print(omegau[i]   +SEPARATOR);
            	pw.print(theta [i][0]+SEPARATOR);
            	pw.print(p     [i][0]+SEPARATOR);
            	for(int ivar=1;ivar<nvariants;ivar++){
            		pw.print(omega[i][ivar]+SEPARATOR);
            		pw.print(theta[i][ivar]+SEPARATOR);
            		pw.print(p    [i][ivar]+SEPARATOR);
            	}
            	pw.println("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           // Nuevamente aprovechamos el finally para 
           // asegurarnos que se cierra el fichero.
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
	}
	
	private void writeNewInECSV(double[][] fromE, double[][] fromI, double[][] fromIu, double[][] fromHR, double[][] fromHD, double[][] newE, double[][] newC, double[][] newH, double[][] newD, int maxtime, String file){
		String[] basicnames = {"New E from E", "New E From I","New E From Iu","New E From HR","New E From HD","New E (total)","New Cases","New Hos","New Deaths"};
		int len = basicnames.length;
		String[] namescol   = new String[len*(nvariants+1)];
		double[][] results  = new double[len*(nvariants+1)][fromE.length];
		//double[][] results = {fromE,fromI,fromIu,fromHR,fromHD,newE,newC,newH,newD};
		for(int col=0; col<len; col++){
			namescol[col] = basicnames[col];
		}
		for(int i=0; i<nvariants;i++){
			for(int t=0; t<fromE.length;t++){
				results[0][t] = results[0][t] + getColumn(fromE, i)[t];
				results[1][t] = results[1][t] + getColumn(fromI, i)[t];
				results[2][t] = results[2][t] + getColumn(fromIu,i)[t];
				results[3][t] = results[3][t] + getColumn(fromHR,i)[t];
				results[4][t] = results[4][t] + getColumn(fromHD,i)[t];
				results[5][t] = results[5][t] + getColumn(newE,  i)[t];
				results[6][t] = results[6][t] + getColumn(newC,  i)[t];
				results[7][t] = results[7][t] + getColumn(newH,  i)[t];
				results[8][t] = results[8][t] + getColumn(newD,  i)[t];
			}
			results [len*(i+1)+0] = getColumn(fromE, i);
			results [len*(i+1)+1] = getColumn(fromI, i);
			results [len*(i+1)+2] = getColumn(fromIu,i);
			results [len*(i+1)+3] = getColumn(fromHR,i);
			results [len*(i+1)+4] = getColumn(fromHD,i);
			results [len*(i+1)+5] = getColumn(newE,  i);
			results [len*(i+1)+6] = getColumn(newC,  i);
			results [len*(i+1)+7] = getColumn(newH,  i);
			results [len*(i+1)+8] = getColumn(newD,  i);
			namescol[len*(i+1)+0] = basicnames[0]+Integer.toString(i+1);
			namescol[len*(i+1)+1] = basicnames[1]+Integer.toString(i+1);
			namescol[len*(i+1)+2] = basicnames[2]+Integer.toString(i+1);
			namescol[len*(i+1)+3] = basicnames[3]+Integer.toString(i+1);
			namescol[len*(i+1)+4] = basicnames[4]+Integer.toString(i+1);
			namescol[len*(i+1)+5] = basicnames[5]+Integer.toString(i+1);
			namescol[len*(i+1)+6] = basicnames[6]+Integer.toString(i+1);
			namescol[len*(i+1)+7] = basicnames[7]+Integer.toString(i+1);
			namescol[len*(i+1)+8] = basicnames[8]+Integer.toString(i+1);
		}
		writeCSV(namescol, results, maxtime, file);
	}
	
	private void writeCausesCSV(double[][] causeE, double[][] causeI, double[][] causeIu, double[][] causeH, int maxtime, String file){
		String[] basicnames = {"Caused by E", "Caused by I","Caused by Iu","Caused by H"};
		int len = basicnames.length;
		String[] namescol   = new String[len*(nvariants+1)];
		double[][] results  = new double[len*(nvariants+1)][causeE.length];
		//double[][] results = {causeE,causeI,causeIu,causeH};
		for(int col=0; col<len; col++){
			namescol[col] = basicnames[col];
		}
		for(int i=0; i<nvariants;i++){
			for(int t=0; t<causeE.length;t++){
				results[0][t] = results[0][t] + getColumn(causeE, i)[t];
				results[1][t] = results[1][t] + getColumn(causeI, i)[t];
				results[2][t] = results[2][t] + getColumn(causeIu,i)[t];
				results[3][t] = results[3][t] + getColumn(causeH,i)[t];
			}
			results [len*(i+1)+0] = getColumn(causeE, i);
			results [len*(i+1)+1] = getColumn(causeI, i);
			results [len*(i+1)+2] = getColumn(causeIu,i);
			results [len*(i+1)+3] = getColumn(causeH, i);
			namescol[len*(i+1)+0] = basicnames[0]+Integer.toString(i+1);
			namescol[len*(i+1)+1] = basicnames[1]+Integer.toString(i+1);
			namescol[len*(i+1)+2] = basicnames[2]+Integer.toString(i+1);
			namescol[len*(i+1)+3] = basicnames[3]+Integer.toString(i+1);
		}
		writeCSV(namescol, results, maxtime, file);
	}
	
	private void writeDosesVacCSV(double[][] firstdoses, double[][] seconddoses, int maxtime, String file){
		String[] basicnames = {"1st Doses of Vaccine ", "2nd Doses of Vaccine "};
		int len= basicnames.length;
		String[] namescol   = new String[len*(nvaccines)];
		double[][] results  = new double[len*(nvaccines)][firstdoses.length];
		for(int i=0; i<nvaccines;i++){
			results [len*i+0] = firstdoses [i];
			results [len*i+1] = seconddoses[i];
			namescol[len*i+0] = basicnames [0]+Integer.toString(i+1);
			namescol[len*i+1] = basicnames [1]+Integer.toString(i+1);
		}
		writeCSV(namescol, results, maxtime, file);
	}
	
	private void computeR(String file, int tmax, double[] S, double[][] newE, double[][][] beta, double[][] fatrate, double[][] theta, double[][] p, double[] omegau){
		double betaIDu = 0.0; // No lo utilizamos todavía
		double gammaIDu= 1.0; // No lo utilizamos todavía
		
		// CALCULO DE Re -------------------------------------------------------------------------------
		double[][] Re   = new double[tmax][nvariants];
		double[][] ReE  = new double[tmax][nvariants];
		double[][] ReI  = new double[tmax][nvariants];
		double[][] ReIu = new double[tmax][nvariants];
		double[][] ReHR = new double[tmax][nvariants];
		double[][] ReHD = new double[tmax][nvariants];
		double[]   R0   = new double[nvariants];
		double[] totalRe= new double[tmax];
		double[] sumnewE= new double[tmax];
		
		for (int t=0; t<tmax; t++){
			for (int ivar=0; ivar<nvariants; ivar++){
				sumnewE[t] = sumnewE[t] + newE[t][ivar];
			}
		}
		
		for (int ivar=0; ivar<nvariants; ivar++){
			int t = 0;
			while((Math.round(t+(sX[0][t]+sX[1][t]+sX[2][t])/dt)<tmax-20)&&(Math.round(t+Math.round(sX[0][t]+sX[1][t]+1/gammaIDu)/dt)<tmax-20)&&
					(Math.round(t+Math.round(sX[0][t]+sX[1][t]+sX[3][t])/dt)<tmax-20)&&(Math.round(t+Math.round(sX[0][t]+sX[1][t]+sX[4][t])/dt)<tmax-20))	{
				
				int nint=(int) Math.floor((sX[0][t])/dt);
				double[] timeE = new double[nint+1];
				double[] fE    = new double[nint+1];
				for(int i=0;i<=nint; i++){
					timeE[i]= t+i;
					fE [i]  = beta[(int)Math.floor(2*timeE[i])][0][ivar]*S[(int)Math.floor(timeE[i])]/totalpop;
					timeE[i]= timeE[i]*dt; 
				}
				
				nint=(int) Math.floor((sX[1][t])/dt);
				double[] timeI = new double[nint+1];
				double[] fI    = new double[nint+1];
				for(int i=0;i<=nint; i++){
					timeI[i]= t+(sX[0][t])/dt+i;
					fI [i]  = beta[(int)Math.floor(2*timeI[i])][1][ivar]*S[(int)Math.floor(timeI[i])]/totalpop;
					timeI[i]= timeI[i]*dt;
				}
				
				nint=(int) Math.floor((sX[2][t])/dt);
				double[] timeIu = new double[nint+1];
				double[] fIu    = new double[nint+1];
				for(int i=0;i<=nint; i++){
					timeIu[i]= t+(sX[0][t]+sX[1][t])/dt+i;
					fIu [i]  = beta[(int)Math.floor(2*timeIu[i])][2][ivar]*S[(int)Math.floor(timeIu[i])]/totalpop;
					timeIu[i]= timeIu[i]*dt;
				}
				
				nint=(int) Math.floor((1/gammaIDu)/dt);
				double[] timeIdu = new double[nint+1];
				double[] fIdu    = new double[nint+1];
				for(int i=0;i<=nint; i++){
					timeIdu[i]= t+(sX[0][t]+sX[1][t])/dt+i;
					fIdu [i]  = betaIDu*S[(int)Math.floor(timeIdu[i])]/totalpop;
					timeIdu[i]= timeIdu[i]*dt;
				}
				
				nint=(int)Math.floor((sX[3][t])/dt);
				double[] timeHR = new double[nint+1];
				double[] fHR    = new double[nint+1];
				for(int i=0;i<=nint; i++){
					timeHR[i]= t+(sX[0][t]+sX[1][t])/dt+i;
					fHR [i]  = beta[(int)Math.floor(2*timeHR[i])][3][ivar]*S[(int)Math.floor(timeHR[i])]/totalpop;
					timeHR[i]= timeHR[i]*dt;
				}
				
				nint=(int) Math.floor((sX[4][t])/dt);
				double[] timeHD = new double[nint+1];
				double[] fHD    = new double[nint+1];
				for(int i=0;i<=nint; i++){
					timeHD[i]= t+(sX[0][t]+sX[1][t])/dt+i;
					fHD [i]  = beta[(int)Math.floor(2*timeHD[i])][4][ivar]*S[(int)Math.floor(timeHD[i])]/totalpop;
					timeHD[i]= timeHD[i]*dt;
				}
				
				int tfts = (int)(2*Math.floor(t+(sX[0][t]+sX[1][t])/dt));
				Re[t][ivar] = simpson(timeE,fE)+simpson(timeI,fI)+(1-theta[tfts][ivar]-omegau[tfts])*simpson(timeIu,fIu)+
						omegau[tfts]*simpson(timeIdu,fIdu)+p[tfts][ivar]*(theta[tfts][ivar]-fatrate[tfts][ivar])*simpson(timeHR,fHR)+
						fatrate[tfts][ivar]*simpson(timeHD,fHD);//
	
				ReE [t][ivar] = simpson(timeE,fE);//
				ReI [t][ivar] = simpson(timeI,fI);//
				ReIu[t][ivar] = (1-theta[tfts][ivar]-omegau[tfts])*simpson(timeIu,fIu);//
				ReHR[t][ivar] = p[tfts][ivar]*(theta[tfts][ivar]-fatrate[tfts][ivar])*simpson(timeHR,fHR);//
				ReHD[t][ivar] = fatrate[tfts][ivar]*simpson(timeHD,fHD);//
				
				t++;
			
			}
		
		
			// CALCULO R0 -----------------------------------------------------------------------------------
			R0[ivar] = Re[0][ivar];
			//-----------------------------------------------------------------------------------------------
		}
		
		// CALCULO Re total
		for (int t=0; t<tmax; t++){
			for (int ivar=0; ivar<nvariants; ivar++){
				totalRe[t] = totalRe[t] + (newE[t][ivar]*Re[t][ivar])/sumnewE[t];
				if (newE[t][ivar]==0){
					Re  [t][ivar] = 0.0;
					ReE [t][ivar] = 0.0;
					ReI [t][ivar] = 0.0;
					ReIu[t][ivar] = 0.0;
					ReHR[t][ivar] = 0.0;
					ReHD[t][ivar] = 0.0;
				}
			}
		}
		//-----------------------------------------------------------------------------------------------
		
		// IMPRIMIMOS A FICHERO CSV:-----------------------------------------------------------------------------
		String[] basicnames = {"Total Re", "Re", "Re E", "Re I", "Re Iu", "Re HR", "Re HD"};
		int len = basicnames.length;
		//double[][] results = {Re,ReE,ReI,ReIu,ReHR,ReHD};
		String[] namescol   = new String[1+(len-1)*nvariants];
		double[][] results  = new double[1+(len-1)*nvariants][tmax];
		results [0] = totalRe; 
		namescol[0] = basicnames[0];
		for(int i=0; i<nvariants;i++){
			results [(len-1)*i+1] = getColumn(Re,  i);
			results [(len-1)*i+2] = getColumn(ReE, i);
			results [(len-1)*i+3] = getColumn(ReI, i);
			results [(len-1)*i+4] = getColumn(ReIu,i);
			results [(len-1)*i+5] = getColumn(ReHR,i);
			results [(len-1)*i+6] = getColumn(ReHD,i);
			namescol[(len-1)*i+1] = basicnames[1]+Integer.toString(i+1);
			namescol[(len-1)*i+2] = basicnames[2]+Integer.toString(i+1);
			namescol[(len-1)*i+3] = basicnames[3]+Integer.toString(i+1);
			namescol[(len-1)*i+4] = basicnames[4]+Integer.toString(i+1);
			namescol[(len-1)*i+5] = basicnames[5]+Integer.toString(i+1);
			namescol[(len-1)*i+6] = basicnames[6]+Integer.toString(i+1);
		}
		writeCSV(namescol, results, tmax, file+"\\Re.csv");
        //----------------------------------------------------------------------------------------------------
		
	}
	
	private double simpson(double[] x, double[] y){
		double integral = 0.0;
		for (int n=0; n<x.length-2; n=n+2){
			integral = integral + (x[n+2]-x[n])*(y[n]+4*y[n+1]+y[n+2]);
		}
		integral = integral/6.0;
		return integral;
	}
	
	private double[] getColumn(double[][] matrix, int ncol){
		double[] column = new double[matrix.length];
		for (int r=0; r<column.length; r++){
			column[r] = matrix[r][ncol];
		}
		return column;
	}
	
	private double[][] getMatrix(double[][][] matrix3d, int index){
		double[][] matrix2d = new double[matrix3d.length][matrix3d[0].length];
		for (int i=0; i<matrix3d.length; i++){
			for (int j=0; j<matrix3d[0].length; j++){
				matrix2d[i][j] = matrix3d[i][j][index];
			}
		}
		return matrix2d;
	}
}
