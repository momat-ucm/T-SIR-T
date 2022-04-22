Description of the columns in controlmeasures.csv:

DATE: Date of application of a new control measure
m: Intensity of the control measures (lower value implies higher intensity)
kappa: Efficiency of the control measures (higher value implies faster effectiveness of the control measures)

If any row contains the keyword "adaptative", then it is considered the date in this row as the first date of application of an adaptative control measure strategy based on the cumulative incidence observed in the last 14 days. In this case, the two following rows contain: 
  the inferior threshold under which a relaxation measure will be applied; the intensity of this control measure "m"; the efficiency of this control measure "kappa"
  the superior threshold above which a restriction measure will be applied; the intensity of this control measure "m"; the efficiency of this control measure "kappa"