# T-SIR Type Model
&theta;-SIR Type Model

Workgroup:

Miriam R. Ferrández (B), Benjamin Ivorra (A), María Vela-Pérez (A), Ángel Manuel Ramos (A)

Institutions: 

 (A) MOMAT research group, IMI-Institute & Applied Mathematics Department. Complutense University of Madrid, Spain.
 
 (B) SAL research group, ceiA3 & Dept. of Computer Science. University of Almería, Spain.

Emails:
 mrferrandez@ual.es, ivorra@ucm.es, maria.vela@ucm.es, angel@mat.ucm.es
 

Details about the model:

T-SIR-T (&theta;-SIR Type Model) is a general deterministic epidemiological model to study the evolution of human diseases in a particular territory. The main characteristics of T-SIR-T are: 
- the inclusion of the biological and sociological mechanisms influencing the disease spread;
- its parameters have a clear interpretation in terms of those mechanisms (such as a contact rate, fatality ratios, duration of infection, etc.); 
- the consideration of the effect of undetected infected people;
- the incorporation of the effect of different sanitary and infectiousness conditions of hospitalized people;
- the estimation of the needs of beds in hospitals;
- the inclusion of the effect of different control measures that can be increased or relaxed  (such as social distancing or sanitary measures).

It has been originally designed and applied to simulate the dynamics of the COVID-19 pandemic incorporating all its special characteristics (undetected cases, undetected deaths, quarantine, social distancing, etc.)

 For more details about the model and its parameters please refer to the following Preprint:
 A simple but complex enough θ-SIR type model to be used with COVID-19 real data. Application to the case of Italy.
 A.M. Ramos, M.R. Ferrández, M. Vela-Pérez, B. Ivorra.
 July 2020.
 
 DOI: 10.13140/RG.2.2.32466.17601

 How to run the model?

1) Download the code; maintaining the organization and the names of the folders and files. 

2) Check that the folder "Scenarios" contains another folder with the name of the country (here, Italy). In this folder, there must be three CSV files that contain the input data:
   -  controlmeasures.csv
   -  disease.csv
   -  timeseries.csv
   There, the user can change some input parameters about the disease and the control measures and also can update the time series with the data reported by the country authorities. 

3) Double click on the Java executable jar file (here, runItaly.jar).

4) Check the obtained results in the folder "Output", within a subfolder named as the country. The numerical results are organized in different CSV files.

5) If you have a MATLAB license, launch MATLAB and run the script allplots.m to obtain some plots of some results.  
