close all;clear all;clc;
currentFolder = pwd;

% Source REPORTED DATA:
T=readtable(strcat(currentFolder,'\Scenarios\Italy\6\timeseries.csv'));
dates  = table2array(T(:,1));
realcc = table2array(T(:,2));
realcd = table2array(T(:,3));
realHos= table2array(T(:,4));
realQ  = table2array(T(:,5));
realR  = table2array(T(:,6));
Tcm=readtable(strcat(currentFolder,'\Scenarios\Italy\9\controlmeasures.csv'));

for i=1:4
    basedir{i} = strcat(currentFolder,'\Output\Italy\',num2str(i+5));
    Tstates{i} = readtable(strcat(basedir{i},'\states.csv'));
    TRe{i}     = readtable(strcat(basedir{i},'\Re.csv'));
    Tnewin{i}  = readtable(strcat(basedir{i},'\newin.csv'));
    Tfeatures{i}=readtable(strcat(basedir{i},'\features.csv'));
    Tcumulat{i} =readtable(strcat(basedir{i},'\cumulative.csv'));
    Tcauses{i}  =readtable(strcat(basedir{i},'\causesOfInfection.csv'));
    Tdoses{i}   =readtable(strcat(basedir{i},'\dosesOfVaccines.csv'));
end

% To obtain the time step and the dates:
datesSimulation = table2array(Tstates{1}(:,1));
datei = datesSimulation(1);
datef = datesSimulation(end);
steps = floor(length(datesSimulation)/(datenum(datef)-datenum(datei)));
dt = 1.0/steps;
sizerealcc=length(realcc)/dt;

nmax=length(datesSimulation);
time=linspace(0.0,nmax-1,nmax);
tline=datenum(datei)+time*dt;
tvec=linspace(tline(1),tline(nmax),10);

plotlines= {'-', '--', '-.', ':'};
colores  = {[0.8500 0.3250 0.0980],[0.9290 0.6940 0.1250],[0.4940 0.1840 0.5560],[0.4660 0.6740 0.1880]};
coloresvar={[0.65 0.65 0.65], [1 0 1], [0.3010 0.7450 0.9330], [0.6350 0.0780 0.1840]};

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%% FIGURE 1   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% New Cases  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

realccNew(1)=0.0;
for i=2:length(realcc)
    realccNew(i)=max(realcc(i)- realcc(i-1),0);
end

figure(1)
subplot(2,2,1)
bar(tline(1:1/dt:sizerealcc), realccNew);
hold on
for i=1:4
    CCNew{i} = table2array(Tnewin{i}(:,8));
    plot(tline(1:end-1),CCNew{i}(1:end-1),plotlines{i},'linewidth',3)
end
title('New cases')
legend('Rep.','Est. with m_{19}=m_{18}=0.15','Est. with m_{i} adaptative, i\geq19','Est. with m_{19}=0.3','Est. with m_{19}=0.4','Location','NorthWest')
ylabel('Number of people/day','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%      New Deaths %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

realcdNew(1)=0.0;
for i=2:length(realcd)
    realcdNew(i)=max(realcd(i)- realcd(i-1),0);
end

figure(1)
subplot(2,2,2)
bar(tline(1:1/dt:sizerealcc), realcdNew);
hold on
for i=1:4
    CDNew{i} = table2array(Tnewin{i}(:,10));
    plot(tline(1:end-1),CDNew{i}(1:end-1),plotlines{i},'linewidth',3)
end
title('New deaths')
legend('Rep.','Est. with m_{19}=m_{18}=0.15','Est. with m_{i} adaptative, i\geq19','Est. with m_{19}=0.3','Est. with m_{19}=0.4','Location','NorthWest')
ylabel('Number of people/day','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)

hfig = figure(2);
pos = get(hfig,'position');
set(hfig,'position',pos.*[.5 1 2 1]);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%  Social Distancing Measures %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
figure(1)
subplot(2,2,3:4)
for i=1:4
    control{i} = table2array(Tfeatures{i}(:,13));
    plot(tline(1:end),control{i}(1:2:end),plotlines{i},'Color',colores{i},'linewidth',3) ;
    hold on;
end
title('Est. social distancing measures')
legend('Est. with m_{19}=m_{18}=0.15','Est. with m_{i} adaptative, i\geq19','Est. with m_{19}=0.3','Est. with m_{19}=0.4','Location','NorthWest')
ylabel('m','fontsize',12)

axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)
ylim([0.0,1.0])

cmdates = table2array(Tcm(:,1));

for i=2:min(length(cmdates),20)
    datel=datestr(cmdates(i));
    xajuste=datenum(datel);
    xla=xline(xajuste,'-.k', datel,'linewidth',2);
    xla.LabelVerticalAlignment = 'top';

    set( get( get( xla, 'Annotation'), 'LegendInformation' ), 'IconDisplayStyle', 'off' );
    legend show
end


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%% FIGURE 2   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% New Cases and New Deaths %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
i=2;
newC41 = table2array(Tnewin{i}(:,17)); 
newC42 = table2array(Tnewin{i}(:,26)); 

newD41 = table2array(Tnewin{i}(:,19)); 
newD42 = table2array(Tnewin{i}(:,28)); 

figure(2)
subplot(2,2,1)
hbar = bar(tline(1:1/dt:end-1),[newC41(1:1/dt:end-1) newC42(1:1/dt:end-1)],'stacked');
hbar(1).FaceColor = 'flat';
hbar(1).CData = coloresvar{1};
hbar(2).FaceColor = 'flat';
hbar(2).CData = coloresvar{2};
title('New cases')
legend('Reference variant', 'New variant','Location','NorthEast')
ylabel('Number of people','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;
tvec2=linspace(tline(tline(:)==datenum('01-Nov-2020')),tline(nmax),10);
xlim([tline(tline(:)==datenum('01-Nov-2020')),max(tline)])
xticks(tvec2)
xticklabels(datestr(tvec2,'dd mmm yyyy'))
xtickangle(45)

figure(2)
subplot(2,2,2)
hbar = bar(tline(1:1/dt:end-1),[newD41(1:1/dt:end-1) newD42(1:1/dt:end-1)],'stacked');
hbar(1).FaceColor = 'flat';
hbar(1).CData = coloresvar{3};
hbar(2).FaceColor = 'flat';
hbar(2).CData = coloresvar{4};
title('New deaths')
legend('Reference variant', 'New variant','Location','NorthEast')
ylabel('Number of people','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;
xlim([tline(tline(:)==datenum('01-Nov-2020')),max(tline)])
xticks(tvec2)
xticklabels(datestr(tvec2,'dd mmm yyyy'))
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% 14-days Cumulative Incidence %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
figure(2)
subplot(2,2,3)
i=2;
CI14{i} = table2array(Tcumulat{i}(:,17));
plot(tline(1:end-1),CI14{i}(1:end-1),plotlines{i},'Color',colores{i},'linewidth',3) ;
title('14-day cumulative incidence')
ylabel('Cumulative incidence','fontsize',12)
legend('Est. with m_{i} adaptative, i\geq19','Location','NorthEast')

ylaSup=yline(250,'-.k','linewidth',2);
ylaInf=yline(150,'-.k','linewidth',2);
set( get( get( ylaSup, 'Annotation'), 'LegendInformation' ), 'IconDisplayStyle', 'off' );
set( get( get( ylaInf, 'Annotation'), 'LegendInformation' ), 'IconDisplayStyle', 'off' );
legend show

axis tight
yy=ylim;
xx=xlim;
grid on;
xlim([tline(tline(:)==datenum('01-Nov-2020')),max(tline)])
ylim([0.0,1000])
%xticks(tvec)
xticks(tvec2)
xticklabels(datestr(tvec,'dd mmm yyyy'))
%xlim([min(tline),max(tline)])
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Re %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

for i=1:4
    Re{i} = table2array(TRe{i}(:,2));
end

i=2;
RePond4 = table2array(TRe{i}(:,2));
Re41   = table2array(TRe{i}(:,3));
Re42   = table2array(TRe{i}(:,9));

figure(2)
subplot(2,2,4)
jini=0;
for jend=1:length(RePond4)
    if (RePond4(jend)==0)
        break;
    end
    if(Re42(jend)==0)
        jini=jini+1;
    end
end

plot(tline(1:jend-1),RePond4(1:jend-1),'--','linewidth',4,'Color',colores{i})
hold on
plot(tline(1:jend-1),Re41(1:jend-1),'-.','linewidth',3,'Color',coloresvar{3})
plot(tline(jini+1:jend-1),Re42(jini+1:jend-1),':','linewidth',3,'Color',coloresvar{4})
legend('With 2nd variant: Re','With 2nd variant: Re^{(1)}','With 2nd variant: Re^{(2)}','Location','NorthEast')
title('Re')
ylabel('Re','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;
xlim([tline(tline(:)==datenum('01-Nov-2020')),max(tline)])
ylim([0.0,2.4])
xticks(tvec2)
xticklabels(datestr(tvec2,'dd mmm yyyy'))
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%% FIGURE 3   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Doses of vaccines %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
figure(3)
subplot(1,2,1)
onedose = table2array(Tdoses{1}(:,2))+table2array(Tdoses{1}(:,4))+table2array(Tdoses{1}(:,6))+table2array(Tdoses{1}(:,8));
fullyvac= table2array(Tdoses{1}(:,3))+table2array(Tdoses{1}(:,5))+table2array(Tdoses{1}(:,7))+table2array(Tdoses{1}(:,8));
plot(tline(1:end), onedose(1:end),':', 'linewidth',3);
hold on;
plot(tline(1:end),fullyvac(1:end),'--','linewidth',3);
legend('At least one dose','Fully vaccinated','Location','NorthWest')
ylabel('Number of people','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;
xlim([tline(tline(:)==datenum('01-Nov-2020')),max(tline)])
xticks(tvec2)
xticklabels(datestr(tvec2,'dd mmm yyyy'))
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Re %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
figure(3)
subplot(1,2,2)
for i=1:4
    plot(tline(1:jend-1),Re{i}(1:jend-1),plotlines{i},'Color',colores{i},'linewidth',3) ;
    hold on;
end
legend('Est. with m_{19}=m_{18}=0.15','Est. with m_{i} adaptative, i\geq19','Est. with m_{19}=0.3','Est. with m_{19}=0.4','Location','NorthWest')
title('Re')
ylabel('Re','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;
xlim([tline(tline(:)==datenum('01-Nov-2020')),max(tline)])
ylim([0.0,2.4])
xticks(tvec2)
xticklabels(datestr(tvec2,'dd mmm yyyy'))
xtickangle(45)

hfig = figure(3);
pos = get(hfig,'position');
set(hfig,'position',pos.*[.5 1 2 1]);