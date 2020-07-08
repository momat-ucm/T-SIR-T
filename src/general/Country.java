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
	//states = {S[t],E[t],I[t],Iu[t],HR[t], HD[t], Q[t]};
	String dfile     = "disease.csv";
	public double[] csvdur = new double[numstates-1];
	public double csvbetai ;
	public double[] csvcoef   = new double[2];
	public String[] csvdatesch= new String[1];
	public double[] csvFR     = new double[3];
	public double csvp ;
	
	// 2) Control measures
	int ncm = 8; // Max. number of different control measures or different levels of control measures
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
	// repdata[t][8] = datoUnD[t];
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
	
	public Country(String iname){
		path = currentFolder + "\\Scenarios\\" +iname+"\\";
		outputpath = currentFolder + "\\Output\\" +iname+"\\";
		readDisease();
		numerrors= 4;
		dt=1.0/6.0;
		setSimulation(datei, datef, dt);
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
	        		repUnD[cntund] = repdata[id][8];
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
				else if (repdata[id][8]>0){
					tund   = new int   [cntund+1];
					repUnD = new double[cntund+1];
					repUnD[cntund] = repdata[id][8];
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
			while(line!=null){
				String[] vline  = line.split(SEPARATOR); 
				datecm  [numlin]= vline[0];
				csvm    [numlin+1]= Double.parseDouble(vline[1]);
				csvkappa[numlin+1]= Double.parseDouble(vline[2]);
				line = br.readLine();
				numlin++;
			}
			ngivencm = numlin; 
			br.close();
			
			// 2) Fill the remaining dates until compete the maximum control measure dates ncm
			while (ncm-ngivencm>0){
				datecm[ngivencm] = "01-Jan-2050";
				ngivencm++;
			}
			
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
			
			// 4th line: 
			line  = br.readLine(); 
			vline = line.split(SEPARATOR);
			int nchanges = 0;
			for(int i=1;i<vline.length; i++){
				if(!vline[i].equals("")){
					csvdatesch[nchanges]=vline[i];
					nchanges++;
				}
			}
			
			// 5th line: Durations;input_dure;input_dure;input_dure;input_dure;input_dure;input_dure;input_dure
			csvdur = new double[numstates-1+nchanges];
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
	
	public double[] evaluate (double delay, double tdur, double[] dur, double varduri, double betai, double[] coef, double[] kappa, double[] m, double[] kappasanit, double[] msanit, double[] omega, double pcte) {
		
		double [] relerror = new double[numerrors] ; // Relative error committed in each time series
		
		// Matrix initialization --------------------------------------------------------------------------
		int tmax = (int) ((dmax-1)/dt)+1; // Number of model time steps
		int thistC = (int) ((NhistC-1)/dt)+1; // Number of model time steps (reported data)
		int thistmax= Math.max(tmax, thistC);
		int delayini=(int)(delay/dt);
		int tsdur = (int)(tdur/dt);
		double[][] states  = new double[tmax][numstates]; // initialize states matrix
		//states = {S[t],E[t],I[t],Iu[t],HR[t], HD[t], Q[t]};
		double[] H  = new double[tmax]; // initialize Hospitalized matrix (H=HR+HD)
		double[] Q  = new double[tmax]; // initialize Quarantine matrix (Q = states[:][numstates-1])
		double[] R  = new double[tmax]; // initialize Recovered matrix
		double[] D  = new double[tmax]; // initialize Death matrix
		double[] Du = new double[tmax]; // initialize Death Undetected matrix
		double[] CC = new double[tmax]; // initialize Cumulative Cases matrix
		double[] CD = new double[tmax]; // initialize Cumulative Deaths matrix
		//-----------------------------------------------------------------------------------------------
		
		// Matrix initialization for PARAMETERS DEPENDING ON TIME ----------------------------------------
		// multiplicamos por dos el tiempo para poder hacer RK4 en t+0.5
		double[][] beta = new double[2*tmax+1][numstates-2]; // beta = {mbetae, mbetai, mbetaiu, mbetahr, mbetahd}
		double[][] gamma= new double[2*thistmax+1][numstates-1]; // gamma= {gammae, gammai, gammaiu, gammahr, gammahd, gammaq}
		double[][] cmeasures= new double[2*thistmax+1][2]; // cmeasures = {SOCIAL control measures mc(t), SANITARY control measures mcsanit(t)}
		double[] frate   = new double[2*thistmax+1];
		double[] theta   = new double[2*tmax+1];
		double[] p       = new double[2*tmax+1];
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
		states[delayini][1] = repdata[(int) Math.floor(delayini*dt)][6]; //repdata[t][6] = datoImpE[t];
		states[delayini][0] = totalpop - states[delayini][1];
		//----------------------------------------------------------------------------------------------------------
		
		// STRATEGY TO COMPUTE THETA:
		for (double t=delayini; t<thistmax+0.5; t=t+0.5){
			cmeasures = evaluateControlMeasures(cmeasures, t, kappa, m, kappasanit, msanit);
			gamma[(int)(2*t)] = evaluateGamma(t, tsdur, dur, cmeasures[(int)(2*t)][1], varduri);
			frate[(int)(2*t)] = cmeasures[(int)(2*t)][1]*omega[1] + (1-cmeasures[(int)(2*t)][1])*omega[0];
		}
		double[] repfrateef = computeCFR(gamma);
		//----------------------------------------------------------------------------------------------------------
		
		double[] etat = interpLineal(computeEta((int)Math.round(1/gamma[0][0]+1/gamma[0][1])));
		
		// CALCULO PARAMETROS EN t inicial (se guardan en la posición 2*delayini)-----------------------------------
		int tthetai = (int) ((repfrateef[NhistC]+6)/dt);// tthetaini = cfr[NhistC]+6
		theta[2*delayini] = evaluateTheta(delayini, delayini, frate, repfrateef);
		omegau[2*delayini]= evaluateOmegaU(delayini, omega[2]);
		p    [2*delayini] = evaluateP(pcte, theta[2*delayini], theta[2*delayini], frate[2*tthetai], frate[2*delayini]);
		beta [2*delayini] = evaluateBeta(betai, coef, cmeasures[2*delayini], gamma[2*delayini], frate[2*delayini], theta[2*delayini], p[2*delayini], omegau[2*delayini], etat[2*delayini]);
		//----------------------------------------------------------------------------------------------------------
		
		for(int t=delayini; t<tmax-1; t++){
			
			// CALCULO PARAMETROS EN t+0.5 (se guardan en la posición 2*t+1)------------------------------------------
			theta[2*t+1] = evaluateTheta(t, t+0.5, frate, repfrateef);
			omegau[2*t+1]= evaluateOmegaU(t+0.5, omega[2]);
			p    [2*t+1] = evaluateP(pcte, theta[2*delayini], theta[2*t+1], frate[2*tthetai], frate[2*t+1]);
			beta [2*t+1] = evaluateBeta(betai, coef, cmeasures[2*t+1], gamma[2*t+1], frate[2*t+1], theta[2*t+1], p[2*t+1], omegau[2*t+1], etat[2*t+1]);
			//----------------------------------------------------------------------------------------------------------
			
			// CALCULO PARAMETROS EN t+1 (se guardan en la posición 2*(t+1))------------------------------------------
			theta[2*t+2] = evaluateTheta(t, t+1, frate, repfrateef);
			omegau[2*t+2]= evaluateOmegaU(t+1, omega[2]);
			p    [2*t+2] = evaluateP(pcte, theta[2*delayini], theta[2*t+2], frate[2*tthetai], frate[2*t+2]);
			beta [2*t+2] = evaluateBeta(betai, coef, cmeasures[2*t+2], gamma[2*t+2], frate[2*t+2], theta[2*t+2], p[2*t+2], omegau[2*t+2], etat[2*t+2]);
			//----------------------------------------------------------------------------------------------------------

			double[] fsyseval = systemf(t, states[t], beta[2*t], gamma[2*t], frate[2*t], theta[2*t], p[2*t], omegau[2*t]);
			
			// RUNGE KUTTA 4 etapas y orden 4 
			double[] statesF2 = new double [numstates];
			for (int s=0; s<numstates; s++){
				statesF2[s] = states[t][s] + dt/2*fsyseval[s];
			}

			double[] fsysevalF2 = systemf(t+1/2, statesF2, beta[2*t+1], gamma[2*t+1], frate[2*t+1], theta[2*t+1], p[2*t+1], omegau[2*t+1]);
			
			double[] statesF3 = new double [numstates];
			for (int s=0; s<numstates; s++){
				statesF3[s] = states[t][s] + dt/2*fsysevalF2[s];
			}
			  
			double[] fsysevalF3 = systemf(t+1/2, statesF3, beta[2*t+1], gamma[2*t+1], frate[2*t+1], theta[2*t+1], p[2*t+1], omegau[2*t+1]);
			
			double[] statesF4 = new double [numstates];
			for (int s=0; s<numstates; s++){
				statesF4[s] = states[t][s] + dt*fsysevalF3[s];
			}
			
			double[] fsysevalF4 = systemf(t+1, statesF4, beta[2*t+2], gamma[2*t+2], frate[2*t+2], theta[2*t+2], p[2*t+2], omegau[2*t+2]);
			
			for (int s=0; s<numstates; s++){
				states[t+1][s] = states[t][s] + dt/6*(fsyseval[s]+2*fsysevalF2[s]+2*fsysevalF3[s]+fsysevalF4[s]);
			}
			
			H[t+1] = states[t+1][4] + states[t+1][5]; // 4 is HR and 5 is HD
			Q[t+1] = states[t+1][6]; // 6 is Q
			R[t+1] = R[t] + dt/2*(gamma[2*t][5]*Q[t] + gamma[2*t][5]*Q [t+1]);  // gammaq=gamma[5]
			D[t+1] = D[t] + dt/2*(gamma[2*t][4]*states[t][5]+gamma[2*(t+1)][4]*states[t+1][5]); // HD = states[5]; gammahd = gamma[4]
			Du[t+1]= Du[t]+ dt/2*(omegau[2*t]*gamma[2*t][1]*states[t][2]+omegau[2*(t+1)]*gamma[2*(t+1)][1]*states[t+1][2]); 
			
			CC[t+1]= CC[t]+ dt/2*(theta[2*t]*gamma[2*t][1]*states[t][2]+theta[2*(t+1)]*gamma[2*(t+1)][1]*states[t+1][2]); // I= states[2]; gammai = gamma[1]
			CD[t+1]= D[t+1]; 
			
			if(((t+1)%(1/dt)==0)&&((t+1)*dt<NhistC)) {
				states[t+1][1]=states[t+1][1]+repdata[(int) Math.floor((t+1)*dt)][6]; //repdata[t][6] = datoImpE[t];
				states[t+1][4]=states[t+1][4]-repdata[(int) Math.floor((t+1)*dt)][7]; //repdata[t][7] = datoEvac[t];
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
		
		if (tstep<=lambda[1]){
			socialcm = (msocial[0]-msocial[1])*Math.exp(-kappasocial[1]*(tstep-lambda[0])*dt)+msocial[1];
			sanitcm  = (msanit [0]-msanit [1])*Math.exp(-kappasanit [1]*(tstep-lambda[0])*dt)+msanit [1];
		}
		else if (tstep<=lambda[2]){
			socialcm = (cmeas[(int)(2*lambda[1])][0]-msocial[2])*Math.exp(-kappasocial[2]*(tstep-lambda[1])*dt)+msocial[2];
			sanitcm  = (cmeas[(int)(2*lambda[1])][1]-msanit [2])*Math.exp(-kappasanit [2]*(tstep-lambda[1])*dt)+msanit [2];
		}
		else if (tstep<=lambda[3]){
			socialcm = (cmeas[(int)(2*lambda[2])][0]-msocial[3])*Math.exp(-kappasocial[3]*(tstep-lambda[2])*dt)+msocial[3];
			// sanitcm no cambia (idem que trozo anterior)
			sanitcm  = (cmeas[(int)(2*lambda[1])][1]-msanit [2])*Math.exp(-kappasanit [2]*(tstep-lambda[1])*dt)+msanit [2];
		}
		else if (tstep<=lambda[4]){
			socialcm = (cmeas[(int)(2*lambda[3])][0]-msocial[4])*Math.exp(-kappasocial[4]*(tstep-lambda[3])*dt)+msocial[4];
			// sanitcm no cambia (idem que trozo anterior)
			sanitcm  = (cmeas[(int)(2*lambda[1])][1]-msanit [2])*Math.exp(-kappasanit [2]*(tstep-lambda[1])*dt)+msanit [2];
		}
		else if (tstep<=lambda[5]){
			socialcm = (cmeas[(int)(2*lambda[4])][0]-msocial[5])*Math.exp(-kappasocial[5]*(tstep-lambda[4])*dt)+msocial[5];
			// sanitcm no cambia (idem que trozo anterior)
			sanitcm  = (cmeas[(int)(2*lambda[1])][1]-msanit [2])*Math.exp(-kappasanit [2]*(tstep-lambda[1])*dt)+msanit [2];
		}
		else if (tstep<=lambda[6]){
			socialcm = (cmeas[(int)(2*lambda[5])][0]-msocial[6])*Math.exp(-kappasocial[6]*(tstep-lambda[5])*dt)+msocial[6];
			// sanitcm no cambia (idem que trozo anterior)
			sanitcm  = (cmeas[(int)(2*lambda[1])][1]-msanit [2])*Math.exp(-kappasanit [2]*(tstep-lambda[1])*dt)+msanit [2];
		}
		else if (tstep<=lambda[7]){
			socialcm = (cmeas[(int)(2*lambda[6])][0]-msocial[7])*Math.exp(-kappasocial[7]*(tstep-lambda[6])*dt)+msocial[7];
			// sanitcm no cambia (idem que trozo anterior)
			sanitcm  = (cmeas[(int)(2*lambda[1])][1]-msanit [2])*Math.exp(-kappasanit [2]*(tstep-lambda[1])*dt)+msanit [2];
		}
		else {
			socialcm = (cmeas[(int)(2*lambda[7])][0]-msocial[8])*Math.exp(-kappasocial[8]*(tstep-lambda[7])*dt)+msocial[8];
			// sanitcm no cambia (idem que trozo anterior)
			sanitcm  = (cmeas[(int)(2*lambda[1])][1]-msanit [2])*Math.exp(-kappasanit [2]*(tstep-lambda[1])*dt)+msanit [2];
		}
		
		cmeas[(int) (2*tstep)][0] = socialcm; 
		cmeas[(int) (2*tstep)][1] = sanitcm;
		
		return cmeas;
	}
	
	public double[] evaluateGamma(double tstep, int tstepdur, double[] dur, double sanitcm, double varduri){
		double gt = varduri * (1-sanitcm);
		
		double gammae = 1/ dur[0];
		double gammai = 1/(dur[1] - gt); 			// 1/duri;      // Transition rate from I to H  
		double gammaiu= 1/(dur[2] + gt); 			// 1/duriu;
		double gammahr= 1/(dur[3] + gt);  
		double gammahd= 1/(dur[4] + gt);  			// 1/durhd;    // Transition rate from H to D  
		double gammaq = 1/ dur[5];
		
		// if gammaQ has a sudden change 
		if ((tstepdur>0)&&(tstep>tstepdur)){
			gammaq = 1/dur[6];
		}
		
		double[] currentgamma = {gammae, gammai, gammaiu, gammahr, gammahd, gammaq};
		
		return currentgamma;
		
	}
	
	public double[] evaluateBeta(double betai0, double[] coef, double[] cmeas, double[] cgamma, double cfrate, double ctheta, double p, double wu, double ieta){
		double mbetae  = cmeas[0]*coef[0]*betai0;
		
		double mbetai  = cmeas[0]*betai0;
		
		double betainf = coef[1] * betai0;
		double betaiu  = betainf + ((betai0-betainf)/(1-cfrate))*(1-ctheta);
		double mbetaiu = cmeas[0]*betaiu;
		
		double mbetahr = (ieta*((mbetai/cgamma[1]) + (mbetae/cgamma[0])+ (1-ctheta-wu)*(mbetaiu/cgamma[2])))/((1-ieta)*((p*(ctheta-cfrate)/cgamma[3])+cfrate/cgamma[4]));
		double mbetahd = mbetahr;
		
		double[] currentbeta = {mbetae, mbetai, mbetaiu, mbetahr, mbetahd};
		
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
			if ((repdata[d+delay][5]!=0)&&(repdata[d+delay][0]!=crold)){
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
	
	public double[] computeCFR(double[][] gamma){
		double[] icfr = new double[NhistC];
		double[] cfr  = new double[NhistC+2]; // the 2 final positions are to store the firstindex (tiCFR) and the lastindex
		
		int firstindex = 1;
		double drold;
		double crold;
		while ((repdata[firstindex][0]-repdata[firstindex-1][0]==0)||(repdata[(int) (firstindex+1/gamma[(int) (2*(firstindex/dt))][4])][1]==0)){ 
			firstindex++;
		}
		
		drold = repdata[(int) (firstindex+1/gamma[(int) (2*(firstindex/dt))][4])][1];
		crold = repdata[firstindex][0];
		for (int d=0; d<=firstindex; d++){
			icfr[d] = drold/crold;
			cfr [d] = icfr[d];
		}

		int countz = 0;
		for (int d=firstindex+1; d+1/gamma[(int) (2*(d/dt))][4]<NhistC; d++){
			if ((repdata[d][0]!=crold)){
				icfr[d] =(repdata[(int) (d+1/gamma[(int) (2*(d/dt))][4])][1]-drold)/(repdata[d][0]-crold);
				drold   = repdata[(int) (d+1/gamma[(int) (2*(d/dt))][4])][1];
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
		
		for (int d=firstindex+1; d+1/gamma[(int) (2*(d/dt))][4]<NhistC; d++){
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

		if(tstep<=(cfr[NhistC]+6)/dt){     // tthetaini = cfr[NhistC]+6
			ctheta = fr[2*(int) (Math.floor((cfr[NhistC]+6)/dt))]/cfr[(int)(cfr[NhistC]+6)];
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
	
	public double[] systemf(double tstep, double[] SEIIuHRHDQ, double[] beta, double[] gamma, double fatrate, double theta, double p, double omegau){
		double S = SEIIuHRHDQ[0];
		double E = SEIIuHRHDQ[1];
		double I = SEIIuHRHDQ[2];
		double Iu= SEIIuHRHDQ[3];
		double HR= SEIIuHRHDQ[4];
		double HD= SEIIuHRHDQ[5];
		double Q = SEIIuHRHDQ[6];
		
		double mbetae = beta[0];
		double mbetai = beta[1];
		double mbetaiu= beta[2];
		double mbetahr= beta[3];
		double mbetahd= beta[4];
		
		double gammae = gamma[0];
		double gammai = gamma[1];
		double gammaiu= gamma[2];
		double gammahr= gamma[3];
		double gammahd= gamma[4];
		double gammaq = gamma[5];
		
		// Flujos new
		double newe = S*(mbetae*E + mbetai*I + mbetaiu*Iu + mbetahr*HR + mbetahd*HD)/totalpop;
		double newi = gammae * E; 
		double newhid = gammai  * I;
		double newhiu = gammaiu * Iu;
		double newr = gammahr * HR;
		double newd2= gammahd * HD;
		double newq = gammaq  * Q; 
        
		double fS =  - newe ; 		
		double fE = newe - newi ;	
		double fI = newi - newhid; 
		double fIu= (1-theta-omegau)*newhid - newhiu; 
		double fHR= p*(theta-fatrate)*newhid - newr  ;
		double fHD=          fatrate *newhid - newd2 ;
		double fQ = (1-p)*(theta-fatrate)*newhid + newr - newq;
		
		double[] fSEIIuHRHDQ = {fS, fE, fI, fIu, fHR, fHD, fQ};
		
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
	public void evaluatepost (int nfolder, double delay, double tdur, double[] dur, double varduri, double betai, double[] coef, double[] kappa, double[] m, double[] kappasanit, double[] msanit, double[] omega, double pcte) {
		
		double [] relerror = new double[numerrors] ; // Relative error committed in each time series
		
		// Matrix initialization --------------------------------------------------------------------------
		int tmax = (int) ((dmax-1)/dt)+1; // Number of model time steps
		int thistC = (int) ((NhistC-1)/dt)+1; // Number of model time steps (reported data)
		int thistmax= Math.max(tmax, thistC);
		int delayini=(int)(delay/dt);
		int tsdur = (int)(tdur/dt);
		double[][] states  = new double[tmax][numstates]; // initialize states matrix
		//states = {S[t],E[t],I[t],Iu[t],HR[t], HD[t], Q[t]};
		double[] H  = new double[tmax]; // initialize Hospitalized matrix (H=HR+HD)
		double[] Q  = new double[tmax]; // initialize Quarantine matrix (Q = states[:][numstates-1])
		double[] R  = new double[tmax]; // initialize Recovered matrix
		double[] D  = new double[tmax]; // initialize Death matrix
		double[] Du = new double[tmax]; // initialize Death Undetected matrix
		double[] CC = new double[tmax]; // initialize Cumulative Cases matrix
		double[] CD = new double[tmax]; // initialize Cumulative Deaths matrix
		//-----------------------------------------------------------------------------------------------
		
		// Matrix initialization for PARAMETERS DEPENDING ON TIME ----------------------------------------
		// multiplicamos por dos el tiempo para poder hacer RK4 en t+0.5
		double[][] beta = new double[2*tmax+1][numstates-2]; // beta = {mbetae, mbetai, mbetaiu, mbetahr, mbetahd}
		double[][] gamma= new double[2*thistmax+1][numstates-1]; // gamma= {gammae, gammai, gammaiu, gammahr, gammahd, gammaq}
		double[][] cmeasures= new double[2*thistmax+1][2]; // cmeasures = {SOCIAL control measures mc(t), SANITARY control measures mcsanit(t)}
		double[] frate   = new double[2*thistmax+1];
		double[] theta   = new double[2*tmax+1];
		double[] p       = new double[2*tmax+1];
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
		states[delayini][1] = repdata[(int) Math.floor(delayini*dt)][6]; //repdata[t][6] = datoImpE[t];
		states[delayini][0] = totalpop - states[delayini][1];
		//----------------------------------------------------------------------------------------------------------
		
		// POSTPROCESSING -------------------------------------------------------------------------------
		double[] CCtotal = new double[tmax];
		double[] CCu     = new double[tmax];
		double[] causeE  = new double[tmax];
		double[] causeI  = new double[tmax]; // initialize causei matrix
		double[] causeIu = new double[tmax]; // initialize causeiu matrix
		double[] causeH  = new double[tmax]; // initialize causeh matrix
		double[] CHos    = new double[tmax];
		double[] CCuei   = new double[tmax];
		double[] CCtuei  = new double[tmax];
		double[] altasHos= new double[tmax];
		double[] newE    = new double[tmax];
		double[] newH    = new double[tmax];
		double[] newCases= new double[tmax];
		double[] newDeaths=new double[tmax];
		double[] betainE = new double[tmax];
		double[] betainI = new double[tmax];
		double[] betainIu = new double[tmax];
		double[] betainHR = new double[tmax];
		double[] betainHD = new double[tmax];
		double[] S = new double[tmax];
		double[] E = new double[tmax];
		double[] I = new double[tmax];
		double[] Iu= new double[tmax];
		double[] HR= new double[tmax];
		double[] HD= new double[tmax];
		S[delayini] = states[delayini][0]; 
		E[delayini] = states[delayini][1]; 
		I[delayini] = states[delayini][2];
		Iu[delayini]= states[delayini][3]; 
		HR[delayini]= states[delayini][4];
		HD[delayini]= states[delayini][5];
		int timestop = tmax;
		//-----------------------------------------------------------------------------------------------
		
		// STRATEGY TO COMPUTE THETA:
		for (double t=delayini; t<thistmax+0.5; t=t+0.5){
			cmeasures = evaluateControlMeasures(cmeasures, t, kappa, m, kappasanit, msanit);
			gamma[(int)(2*t)] = evaluateGamma(t, tsdur, dur, cmeasures[(int)(2*t)][1], varduri);
			frate[(int)(2*t)] = cmeasures[(int)(2*t)][1]*omega[1] + (1-cmeasures[(int)(2*t)][1])*omega[0];
		}
		double[] repfrateef = computeCFR(gamma);
		//----------------------------------------------------------------------------------------------------------
		
		double[] etat = interpLineal(computeEta((int)Math.round(1/gamma[0][0]+1/gamma[0][1])));
		
		// CALCULO PARAMETROS EN t inicial (se guardan en la posición 2*delayini)-----------------------------------
		int tthetai = (int) ((repfrateef[NhistC]+6)/dt);// tthetaini = cfr[NhistC]+6
		theta[2*delayini] = evaluateTheta(delayini, delayini, frate, repfrateef);
		omegau[2*delayini]= evaluateOmegaU(delayini, omega[2]);
		p    [2*delayini] = evaluateP(pcte, theta[2*delayini], theta[2*delayini], frate[2*tthetai], frate[2*delayini]);
		beta [2*delayini] = evaluateBeta(betai, coef, cmeasures[2*delayini], gamma[2*delayini], frate[2*delayini], theta[2*delayini], p[2*delayini], omegau[2*delayini], etat[2*delayini]);
		//----------------------------------------------------------------------------------------------------------
		
		for(int t=delayini; t<tmax-1; t++){
			
			// CALCULO PARAMETROS EN t+0.5 (se guardan en la posición 2*t+1)------------------------------------------
			theta[2*t+1] = evaluateTheta(t, t+0.5, frate, repfrateef);
			omegau[2*t+1]= evaluateOmegaU(t+0.5, omega[2]);
			p    [2*t+1] = evaluateP(pcte, theta[2*delayini], theta[2*t+1], frate[2*tthetai], frate[2*t+1]);
			beta [2*t+1] = evaluateBeta(betai, coef, cmeasures[2*t+1], gamma[2*t+1], frate[2*t+1], theta[2*t+1], p[2*t+1], omegau[2*t+1], etat[2*t+1]);
			//----------------------------------------------------------------------------------------------------------
			
			// CALCULO PARAMETROS EN t+1 (se guardan en la posición 2*(t+1))------------------------------------------
			theta[2*t+2] = evaluateTheta(t, t+1, frate, repfrateef);
			omegau[2*t+2]= evaluateOmegaU(t+1, omega[2]);
			p    [2*t+2] = evaluateP(pcte, theta[2*delayini], theta[2*t+2], frate[2*tthetai], frate[2*t+2]);
			beta [2*t+2] = evaluateBeta(betai, coef, cmeasures[2*t+2], gamma[2*t+2], frate[2*t+2], theta[2*t+2], p[2*t+2], omegau[2*t+2], etat[2*t+2]);
			//----------------------------------------------------------------------------------------------------------

			double[] fsyseval = systemf(t, states[t], beta[2*t], gamma[2*t], frate[2*t], theta[2*t], p[2*t], omegau[2*t]);
			
			// RUNGE KUTTA 4 etapas y orden 4 
			double[] statesF2 = new double [numstates];
			for (int s=0; s<numstates; s++){
				statesF2[s] = states[t][s] + dt/2*fsyseval[s];
			}

			double[] fsysevalF2 = systemf(t+1/2, statesF2, beta[2*t+1], gamma[2*t+1], frate[2*t+1], theta[2*t+1], p[2*t+1], omegau[2*t+1]);
			
			double[] statesF3 = new double [numstates];
			for (int s=0; s<numstates; s++){
				statesF3[s] = states[t][s] + dt/2*fsysevalF2[s];
			}
			  
			double[] fsysevalF3 = systemf(t+1/2, statesF3, beta[2*t+1], gamma[2*t+1], frate[2*t+1], theta[2*t+1], p[2*t+1], omegau[2*t+1]);
			
			double[] statesF4 = new double [numstates];
			for (int s=0; s<numstates; s++){
				statesF4[s] = states[t][s] + dt*fsysevalF3[s];
			}
			
			double[] fsysevalF4 = systemf(t+1, statesF4, beta[2*t+2], gamma[2*t+2], frate[2*t+2], theta[2*t+2], p[2*t+2], omegau[2*t+2]);
			
			for (int s=0; s<numstates; s++){
				states[t+1][s] = states[t][s] + dt/6*(fsyseval[s]+2*fsysevalF2[s]+2*fsysevalF3[s]+fsysevalF4[s]);
			}
			
			H[t+1] = states[t+1][4] + states[t+1][5]; // 4 is HR and 5 is HD
			Q[t+1] = states[t+1][6]; // 6 is Q
			R[t+1] = R[t] + dt/2*(gamma[2*t][5]*Q[t] + gamma[2*t][5]*Q [t+1]);  // gammaq=gamma[5]
			D[t+1] = D[t] + dt/2*(gamma[2*t][4]*states[t][5]+gamma[2*(t+1)][4]*states[t+1][5]); // HD = states[5]; gammahd = gamma[4]
			Du[t+1]= Du[t]+ dt/2*(omegau[2*t]*gamma[2*t][1]*states[t][2]+omegau[2*(t+1)]*gamma[2*(t+1)][1]*states[t+1][2]); 
			
			CC[t+1]= CC[t]+ dt/2*(theta[2*t]*gamma[2*t][1]*states[t][2]+theta[2*(t+1)]*gamma[2*(t+1)][1]*states[t+1][2]); // I= states[2]; gammai = gamma[1]
			CD[t+1]= D[t+1]; 
			
			if(((t+1)%(1/dt)==0)&&((t+1)*dt<NhistC)) {
				states[t+1][1]=states[t+1][1]+repdata[(int) Math.floor((t+1)*dt)][6]; //repdata[t][6] = datoImpE[t];
				states[t+1][4]=states[t+1][4]-repdata[(int) Math.floor((t+1)*dt)][7]; //repdata[t][7] = datoEvac[t];
			}
			//---------------------------------------------------------------------------------------------------
			
			// POSTPROCESSING -----------------------------------------------------------------------------------
			S[t+1] = states[t+1][0];
			E[t+1] = states[t+1][1];
			I[t+1] = states[t+1][2];
			Iu[t+1]= states[t+1][3];
			HR[t+1]= states[t+1][4];
			HD[t+1]= states[t+1][5];
			
			CCtotal[t+1] = CCtotal[t]+ dt/2*(gamma[2*t][1]*I[t]+gamma[2*(t+1)][1]*I[t+1]);
			causeE[t+1]  = causeE[t] + dt/2*(beta[2*t][0]*E[t]*S[t]+beta[2*(t+1)][0]*E[t+1]*S[t+1])/totalpop;
			causeI[t+1]  = causeI[t] + dt/2*(beta[2*t][1]*I[t]*S[t]+beta[2*(t+1)][1]*I[t+1]*S[t+1])/totalpop;
			causeIu[t+1] = causeIu[t]+ dt/2*(beta[2*t][2]*Iu[t]*S[t]+beta[2*(t+1)][2]*Iu[t+1]*S[t+1])/totalpop;
			causeH[t+1]  = causeH[t] + dt/2*(beta[2*t][3]*H[t]*S[t]+beta[2*(t+1)][3]*H[t+1]*S[t+1])/totalpop;
			altasHos[t+1]= altasHos[t]+dt/2*(gamma[2*t][3]*HR[t] + gamma[2*(t+1)][3]*HR[t+1]);
			CHos[t+1]    = CHos[t] + dt/2*((p[2*t]*theta[2*t]+(1-p[2*t])*frate[2*t])*gamma[2*t][1]*I[t]+(p[2*(t+1)]*theta[2*(t+1)]+(1-p[2*(t+1)])*frate[2*(t+1)])*gamma[2*(t+1)][1]*I[t+1]);
			CCu[t+1]     = CCtotal[t+1]-CC[t+1];
			CCuei[t+1]   = E[t+1]+I[t+1]+CCu[t+1];
			CCtuei[t+1]  = E[t+1]+I[t+1]+CCtotal[t+1];
			
			newCases[t]  = theta[2*t]*gamma[2*t][1]*I[t];
			newDeaths[t] = gamma[2*t][4]*HD[t];
			newH[t]      = (p[2*t]*(theta[2*t]-frate[2*t])+frate[2*t])*gamma[2*t][1]*I[t];
			newE[t]      = (beta[2*t][0]*E[t]+beta[2*t][1]*I[t]+beta[2*t][2]*Iu[t]+beta[2*t][3]*H[t])*S[t]/totalpop;
			betainE[t]   = (beta[2*t][0]*E[t])*S[t]/totalpop;
			betainI[t]   = (beta[2*t][1]*I[t])*S[t]/totalpop;
			betainIu[t]  = (beta[2*t][2]*Iu[t])*S[t]/totalpop;
			betainHR[t]  = (beta[2*t][3]*HR[t])*S[t]/totalpop;
			betainHD[t]  = (beta[2*t][4]*HD[t])*S[t]/totalpop;
			if(((t)%(1/dt)==0)&&((t)*dt<NhistC)) {
				newE[t]=newE[t]+repdata[(int) Math.floor((t)*dt)][6];//-repdata[(int) Math.floor((t)*dt)][7]; //repdata[t][7] = datoEvac[t];
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
		String filePath = outputpath + Integer.toString(nfolder)+"\\";
		File directory = new File(filePath);
	    if (! directory.exists()){
	        directory.mkdirs();
	    }
	    writeStatesCSV(S,E,I,Iu,HR,HD,Q,R,D,Du,timestop,filePath+"states.csv");
	    writeCumulativeCSV(CC,CD,CHos,CCuei,CCtuei,timestop,filePath+"cumulative.csv"); 
	    writeFeaturesCSV(gamma,beta,cmeasures,frate,omegau,theta,p,2*timestop-1,filePath+"features.csv");
	    writeNewInECSV(betainE,betainI,betainIu,betainHR,betainHD,newE,newCases,newH,newDeaths,timestop,filePath+"newin.csv");
	    writeCausesCSV(causeE,causeI,causeIu,causeH,timestop,filePath+"causesOfInfection.csv");
		
		computeR(filePath, timestop, S, beta, gamma, frate, theta, p, omegau);
	}
	
	public void evaluatepost(){
		int nfolder  = 1;
		double delay = 0.0;
		double[] kappasanit = {1.0, 1.0, 1.0};
		double[]     msanit = {1.0, 1.0, 0.0};
		double varduri = 0.0; 
		int tdur=0;
		try{
			Date datech = format.parse(csvdatesch[0]);
			tdur = (int)((Math.round(datech.getTime()/(3600000*24)) - Math.round(dateinit.getTime()/(3600000*24)))); 
		}catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		evaluatepost (nfolder, delay, tdur, csvdur, varduri, csvbetai, csvcoef, csvkappa, csvm, kappasanit, msanit, csvFR, csvp);
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
	
	private void writeStatesCSV(double[] S, double[] E, double[] I, double[] Iu, double[] HR, double[] HD, double[] Q, double[] R, double[] D, double[] Du, int maxtime, String file){
		String [] namescol  = {"S", "E","I","Iu","HR","HD","Q","R","D","Du"};
		double[][] allstates= {S,E,I,Iu,HR,HD,Q,R,D,Du};
		writeCSV(namescol, allstates, maxtime, file);
	}
	
	private void writeCumulativeCSV(double[] cases, double[] deaths, double[] chos, double[] ccuei, double[] cctotalei, int maxtime, String file){
		String [] namescol = {"CC", "CD","CHos","CCuEI","CCtotalEI"};
		double[][] results = {cases,deaths,chos,ccuei,cctotalei};
		writeCSV(namescol, results, maxtime, file);
	}
	
	private void writeFeaturesCSV(double[][] gammas, double[][] betas, double[][] cntrmeas, double[] omega, double[] omegau, double[] theta, double[] p, int maxtime, String file){
		String [] namescol = {"gammae","gammai","gammaiu","gammahr","gammahd","gammaq","mbetae","mbetai","mbetaiu","mbetahr","mbetahd","cmsocial","cmsanit","omega","omegau","theta","p"};
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
            	pw.print(omega[i]+SEPARATOR);
            	pw.print(omegau[i]+SEPARATOR);
            	pw.print(theta[i]+SEPARATOR);
            	pw.print(p[i]+SEPARATOR);
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
	
	private void writeNewInECSV(double[] fromE, double[] fromI, double[]fromIu, double[] fromHR, double[] fromHD, double[] newE, double[] newC, double[] newH, double[] newD, int maxtime, String file){
		String [] namescol = {"New E from E", "New E From I","New E From Iu","New E From HR","New E From HD","New E (total)","New Cases","New Hos","New Deaths"};
		double[][] results = {fromE,fromI,fromIu,fromHR,fromHD,newE,newC,newH,newD};
		writeCSV(namescol, results, maxtime, file);
	}
	
	private void writeCausesCSV(double[] causeE, double[] causeI, double[] causeIu, double[] causeH, int maxtime, String file){
		String [] namescol = {"Caused by E", "Caused by I","Caused by Iu","Caused by H"};
		double[][] results = {causeE,causeI,causeIu,causeH};
		writeCSV(namescol, results, maxtime, file);
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
	
	private void writeStates(double[] S, double[] E, double[] I, double[] Iu, double[] HR, double[] HD, double[] Q, double[] R, double[] D, double[] Du, int maxtime, String file){
		String fileS = file + "S.txt";
		String fileE = file + "E.txt";
		String fileI = file + "I.txt";
		String fileIu = file + "Iu.txt";
		String fileHR = file + "HR.txt";
		String fileHD = file + "HD.txt";
		String fileQ = file + "Q.txt";
		String fileR = file + "R.txt";
		String fileD = file + "D.txt";
		String fileDu= file + "Du.txt";
		writeResult(S, maxtime, fileS);
		writeResult(E, maxtime, fileE);
		writeResult(I, maxtime, fileI);
		writeResult(Iu, maxtime, fileIu);
		writeResult(HR, maxtime, fileHR);
		writeResult(HD, maxtime, fileHD);
		writeResult(Q, maxtime, fileQ);
		writeResult(R, maxtime, fileR);
		writeResult(D, maxtime, fileD);
		writeResult(Du, maxtime, fileDu);
	}
	
	private void writeCumulative(double[] cases, double[] deaths, double[] chos, int maxtime, String file){
		String fileCC = file + "CC.txt";
		String fileCD = file + "CD.txt";
		String fileCH = file + "CHos.txt";
		writeResult(cases, maxtime, fileCC);
		writeResult(deaths, maxtime, fileCD);
		writeResult(chos, maxtime, fileCH);
	}
	
	private void writeCauses(double[] causeE, double[] causeI, double[] causeIu, double[] causeH, int maxtime, String file){
		String fileCauseE = file + "causeE.txt";
		String fileCauseI = file + "causeI.txt";
		String fileCauseIu = file+ "causeIu.txt";
		String fileCauseH = file + "causeH.txt";
		writeResult(causeE, maxtime, fileCauseE);
		writeResult(causeI, maxtime, fileCauseI);
		writeResult(causeIu, maxtime, fileCauseIu);
		writeResult(causeH, maxtime, fileCauseH);
	}
	
	private void writeTotalWithUn(double[] cctotalei, double[] ccuei, int maxtime, String file){
		String fileCCtotalEI = file + "CCtotalEI.txt";
		String fileCCuEI     = file + "CCuEI.txt";
		writeResult(cctotalei, maxtime, fileCCtotalEI);
		writeResult(ccuei, maxtime, fileCCuEI);
	}
	
	private void writeFeatures(double[][] feature, int maxtime, String filepath, String[] featnames){
		int nf = featnames.length;
		String[] files = new String[nf];
		FileWriter[] fichero = new FileWriter[nf];
        PrintWriter[] pw = new PrintWriter[nf];
		try{
			for (int k=0; k<nf; k++){
				files[k]   = filepath + featnames[k];
				fichero[k] = new FileWriter(files[k]);
				pw[k]      = new PrintWriter(fichero[k]);
			}
			for (int k=0; k<nf; k++){
		            for (int t = 0; t < maxtime; t++){
		            	pw[k].println(feature[t][k]);
		            }
			}
			for (int k=0; k<nf; k++){
				fichero[k].close();
			}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           // Nuevamente aprovechamos el finally para 
           // asegurarnos que se cierra el fichero.
        	   for (int k=0; k<nf; k++){  
		           if (null != fichero[k])
		              fichero[k].close();
        	   }
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
	}
	
	private void computeR(String file, int tmax, double[] S, double[][] beta, double[][] gamma,  double[] fatrate, double[] theta, double[] p, double[] omegau){
		double betaIDu = 0.0; // No lo utilizamos todavía
		double gammaIDu= 1.0; // No lo utilizamos todavía
		
		// CALCULO DE Re -------------------------------------------------------------------------------
		double[] Re = new double[tmax];
		double[] ReE = new double[tmax];
		double[] ReI = new double[tmax];
		double[] ReIu = new double[tmax];
		double[] ReHR = new double[tmax];
		double[] ReHD = new double[tmax];
		int t = 0;
		while((Math.round(t+(1/gamma[2*t][0]+1/gamma[2*t][1]+1/gamma[2*t][2])/dt)<tmax-20)&&(Math.round(t+Math.round(1/gamma[2*t][0]+1/gamma[2*t][1]+1/gammaIDu)/dt)<tmax-20)&&
				(Math.round(t+Math.round(1/gamma[2*t][0]+1/gamma[2*t][1]+1/gamma[2*t][3])/dt)<tmax-20)&&(Math.round(t+Math.round(1/gamma[2*t][0]+1/gamma[2*t][1]+1/gamma[2*t][4])/dt)<tmax-20))	{
			
			int nint=(int) Math.floor((1/gamma[2*t][0])/dt);
			double[] timeE = new double[nint+1];
			double[] fE    = new double[nint+1];
			for(int i=0;i<=nint; i++){
				timeE[i]= t+i;
				fE [i]  = beta[(int)Math.floor(2*timeE[i])][0]*S[(int)Math.floor(timeE[i])]/totalpop;
				timeE[i]= timeE[i]*dt; 
			}
			
			nint=(int) Math.floor((1/gamma[2*t][1])/dt);
			double[] timeI = new double[nint+1];
			double[] fI    = new double[nint+1];
			for(int i=0;i<=nint; i++){
				timeI[i]= t+(1/gamma[2*t][0])/dt+i;
				fI [i]  = beta[(int)Math.floor(2*timeI[i])][1]*S[(int)Math.floor(timeI[i])]/totalpop;
				timeI[i]= timeI[i]*dt;
			}
			
			nint=(int) Math.floor((1/gamma[2*t][2])/dt);
			double[] timeIu = new double[nint+1];
			double[] fIu    = new double[nint+1];
			for(int i=0;i<=nint; i++){
				timeIu[i]= t+(1/gamma[2*t][0]+1/gamma[2*t][1])/dt+i;
				fIu [i]  = beta[(int)Math.floor(2*timeIu[i])][2]*S[(int)Math.floor(timeIu[i])]/totalpop;
				timeIu[i]= timeIu[i]*dt;
			}
			
			nint=(int) Math.floor((1/gammaIDu)/dt);
			double[] timeIdu = new double[nint+1];
			double[] fIdu    = new double[nint+1];
			for(int i=0;i<=nint; i++){
				timeIdu[i]= t+(1/gamma[2*t][0]+1/gamma[2*t][1])/dt+i;
				fIdu [i]  = betaIDu*S[(int)Math.floor(timeIdu[i])]/totalpop;
				timeIdu[i]= timeIdu[i]*dt;
			}
			
			nint=(int)Math.floor((1/gamma[2*t][3])/dt);
			double[] timeHR = new double[nint+1];
			double[] fHR    = new double[nint+1];
			for(int i=0;i<=nint; i++){
				timeHR[i]= t+(1/gamma[2*t][0]+1/gamma[2*t][1])/dt+i;
				//System.out.println("tiempo "+t+ "tiempoHR "+timeHR[i]);
				fHR [i]  = beta[(int)Math.floor(2*timeHR[i])][3]*S[(int)Math.floor(timeHR[i])]/totalpop;
				timeHR[i]= timeHR[i]*dt;
			}
			
			nint=(int) Math.floor((1/gamma[2*t][4])/dt);
			double[] timeHD = new double[nint+1];
			double[] fHD    = new double[nint+1];
			for(int i=0;i<=nint; i++){
				timeHD[i]= t+(1/gamma[2*t][0]+1/gamma[2*t][1])/dt+i;
				fHD [i]  = beta[(int)Math.floor(2*timeHD[i])][4]*S[(int)Math.floor(timeHD[i])]/totalpop;
				timeHD[i]= timeHD[i]*dt;
			}
			
			int tfts = (int)(2*Math.floor(t+(1/gamma[2*t][0]+1/gamma[2*t][1])/dt));
			Re[t] = simpson(timeE,fE)+simpson(timeI,fI)+(1-theta[tfts]-omegau[tfts])*simpson(timeIu,fIu)+
					omegau[tfts]*simpson(timeIdu,fIdu)+p[tfts]*(theta[tfts]-fatrate[tfts])*simpson(timeHR,fHR)+
					fatrate[tfts]*simpson(timeHD,fHD);//

			ReE[t] = simpson(timeE,fE);//
			ReI[t] = simpson(timeI,fI);//
			ReIu[t] = (1-theta[tfts]-omegau[tfts])*simpson(timeIu,fIu);//
			ReHR[t] = p[tfts]*(theta[tfts]-fatrate[tfts])*simpson(timeHR,fHR);//
			ReHD[t] = fatrate[tfts]*simpson(timeHD,fHD);//
			
			t++;
			
		}
		
		// CALCULO R0 -----------------------------------------------------------------------------------
		double R0 = Re[0];
		
		//-----------------------------------------------------------------------------------------------
		
		// IMPRIMIMOS A FICHERO CSV:-----------------------------------------------------------------------------
		String [] namescol = {"Re", "Re E","Re I","Re Iu","Re HR","Re HD"};
		double[][] results = {Re,ReE,ReI,ReIu,ReHR,ReHD};
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
	
	
}
