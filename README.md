# T-SIR-T
&theta;-SIR Type Model

## Workgroup

Miriam R. Ferrández (A, B), Benjamin Ivorra (A), María Vela-Pérez (A), Alicja B. Kubik (A), Ángel Manuel Ramos (A)

### Institutions: 

 (A) MOMAT research group (https://www.ucm.es/momat), IMI-Institute & Applied Mathematics Department. Complutense University of Madrid, Spain.
 
 (B) SAL research group (https://sites.google.com/ual.es/hpca), ceiA3 & Dept. of Computer Science. University of Almería, Spain.

### Emails:
 mrferrandez@ual.es, ivorra@ucm.es, maria.vela@ucm.es, akubik@ucm.es, angel@mat.ucm.es
 

## Details about the model

T-SIR-T (&theta;-SIR Type Model) is a general deterministic epidemiological model to study the evolution of human diseases in a particular territory. The main characteristics of T-SIR-T are: 
- the inclusion of the biological and sociological mechanisms influencing the disease spread;
- its parameters have a clear interpretation in terms of those mechanisms (such as a contact rate, fatality ratios, duration of infection, etc.); 
- the consideration of the effect of undetected infected people;
- the incorporation of the effect of different sanitary and infectiousness conditions of hospitalized people;
- the estimation of the needs of beds in hospitals;
- the inclusion of the effect of different control measures that can be increased or relaxed  (such as social distancing or sanitary measures).

It has been originally designed and applied to simulate the dynamics of the COVID-19 pandemic incorporating all its special characteristics (undetected cases, undetected deaths, quarantine, social distancing, etc.).

Some IMPORTANT NOVELTIES incorporated in the version of January 2021 are: 
- the possibility of considering new variants of the virus, with different contact rates;
- the possibility of introducing vaccination campaigns using several vaccines with different characteristics regarding the number of doses and their efficacy. 

 For more details about the model and its parameters please refer to the following Preprints:
 
 ## Preprints 
 [1] A simple but complex enough θ-SIR type model to be used with COVID-19 real data. Application to the case of Italy.
 A.M. Ramos, M.R. Ferrández, M. Vela-Pérez, B. Ivorra.
 July 2020.
 DOI: http://www.doi.org/10.13140/RG.2.2.32466.17601
 
 [2] Modeling the impact of SARS-CoV-2 variants and vaccines onthe spread of COVID-19. 
 A.M. Ramos, M. Vela-Pérez, M.R. Ferrández, A.B. Kubik, B. Ivorra.
 January 2021. Updated May 2021.
 DOI: http://www.doi.org/10.13140/RG.2.2.32580.24967/2

 ## How to run the model?

1) Download the code; keeping the organization and the names of the folders and files. 

2) Check that the folder "Scenarios" contains another folder with the name of the country (here, Italy). Different scenarios are separated into different folders by enumerating them. In each numbered folder, there must be three CSV files that contain the input data:
   -  controlmeasures.csv
   -  disease.csv
   -  timeseries.csv
   -  variant_and_vaccines_estimations.csv(*)
   
   There, the user can change some input parameters about the disease and the control measures and also can update the time series with the data reported by the country authorities. 
   
   (*) It is only needed since the May 2021 version of the model, which includes the improvements detailed in the Annex of Ref. [2].

3) Double click on the Java executable jar file: 
   - runItaly.jar: to execute the Italy scenario number 1, which corresponds to the simulations included in [1];
     or
   - runItaly_VariantsVaccines.jar: to execute Italy scenarios from 2 to 5. They correspond to the simulations included in [2];
     or
   - runItaly_Vaccines_May2021.jar: to execute Italy scenarios from 6 to 9. They correspond to the simulations included in the Annex of [2].

4) Check the obtained results in the folder "Output", within a subfolder named as the country and the number of the scenario. The numerical results are organized in different CSV files.

5) If you have a MATLAB license, launch MATLAB and run the script allplots.m or allplots_VariantVaccines.m or allplots_Vaccines_May2021 to obtain some plots of some results.  
