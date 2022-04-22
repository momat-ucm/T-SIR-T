Description of the rows in disease.csv:

Name: Description of the disease and the country
Country population: Population of the country in "Name"
Initial and final dates: Initial and final dates of the simulation
Date of changes: Dates when the corresponding duration (same column) changes
Date of sudden changes in Durations: Dates when the corresponding duration (same column) changes. If there is a "linear", it means the corresponding duration changes linearly
Starting and final date of linear changes in Durations: In order, initial and final dates of each linear change of the durations, specified in Date of sudden changes in Durations
Durations: Initial duration (given as a suitable average or median) in each compartment E, I, Iu, HR, HD and Q, the inverses of the gammas
BetaI: Value of betaI
Coef Beta: Coefficients multiplying BetaI in order to obtain betaE and betaIu
FR: Case fatality rates. The first value is the instantaneous infection detected fatality ratio (iIdFR) when the implemented control measures are fully applied. The second value is the instantaneous infection detected fatality ratio (iIdFR) when no control measures are applied. The third value is the instantaneous infection undetected fatality ratio (iIuFR).
p: Initial ratio of people that will recover and are hospitalized
k: Incremental coefficient of disease contact rates for new variants. If there is more than one value per new variant, first all the incremental coefficients of disease contact rates for the different variants appear, and then, all the incremental coefficients of the instantaneous infection detected fatality ratio (iIdFR) for the different variants.
vaccines: Number of different vaccines and initial dates of application
days 2nd dose: Necessary days since the administration of the first dose of each vaccine and the second dose
e1: Efficacy of the first vaccine
days e1: First day since the administration of the first dose of vaccine 1 such that its efficacy is the one in the corresponding column
doses_week* (vaccine 1): Doses coming weekly of vaccine 1. Each column correspond to successive weeks, and it remains constant from the last reported value until the end of the simulations
e2: Efficacy of the second vaccine
days e2: First day since the administration of the first dose of vaccine 2 such that its efficacy is the one in the corresponding column
doses_week* (vaccine 2): Doses coming weekly of vaccine 2. Each column correspond to successive weeks, and it remains constant from the last reported value until the end of the simulations
e3: Efficacy of the third vaccine
days e3: First day since the administration of the first dose of vaccine 3 such that its efficacy is the one in the corresponding column
doses_week* (vaccine 3): Doses coming weekly of vaccine 3. Each column correspond to successive weeks, and it remains constant from the last reported value until the end of the simulations
e4: Efficacy of the fourth vaccine
days e4: First day since the administration of the first dose of vaccine 4 such that its efficacy is the one in the corresponding column
doses_week* (vaccine 4): Doses coming weekly of vaccine 4. Each column correspond to successive weeks, and it remains constant from the last reported value until the end of the simulations

(*)Remark: the values in doses_week are taken into account only if there not exists the file "variant_and_vaccines_estimations.csv" containing the estimated daily temporal series of doses 
