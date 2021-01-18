close all;clear all;clc;
currentFolder = pwd;

% Source REPORTED DATA:
T=readtable(strcat(currentFolder,'\Scenarios\Italy\1\timeseries.csv'));
dates  = table2array(T(:,1));
realcc = table2array(T(:,2));
realcd = table2array(T(:,3));
realHos= table2array(T(:,4));
realQ  = table2array(T(:,5));
realR  = table2array(T(:,6));
Tcm=readtable(strcat(currentFolder,'\Scenarios\Italy\1\controlmeasures.csv'));

% Source ESTIMATED DATA:
basedir = strcat(currentFolder,'\Output\Italy\1');
Tstates = readtable(strcat(basedir,'\states.csv'));
TRe     = readtable(strcat(basedir,'\Re.csv'));
Tnewin  = readtable(strcat(basedir,'\newin.csv'));
Tfeatures=readtable(strcat(basedir,'\features.csv'));
Tcumulat =readtable(strcat(basedir,'\cumulative.csv'));
Tcauses  =readtable(strcat(basedir,'\causesOfInfection.csv'));

% To obtain the time step and the dates:
datesSimulation = table2array(Tstates(:,1));
datei = datesSimulation(1);
datef = datesSimulation(end);
steps = floor(length(datesSimulation)/(datenum(datef)-datenum(datei)));
dt = 1.0/steps;
sizerealcc=length(realcc)/dt;

nmax=length(datesSimulation);
time=linspace(0.0,nmax-1,nmax);
tline=datenum(datei)+time*dt;
tvec=linspace(tline(1),tline(nmax),10);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%% FIGURE 1   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% CC and CD   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
CC = table2array(Tcumulat(:,2));
CD = table2array(Tcumulat(:,3));

figure(1)
subplot(2,2,1)
plot((tline(1:1/dt:sizerealcc)),realcc,'linewidth',7,'markersize',5, 'color', [0.75 0.75 0.75])
hold on
plot((tline(1:1/dt:sizerealcc)),realcd,'m:','linewidth',7,'markersize',5)
%
plot(tline,CC,'--b','linewidth',3)
plot(tline,CD,'-.r','linewidth',3)
legend('Rep. Cases','Rep. Deaths','Est. Cases','Est. Deaths','Location','NorthWest')
ylabel('Cumulative number of people','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;
xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% HOSPITALIZED %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
HR = table2array(Tstates(:,6));
HD = table2array(Tstates(:,7));
Hos = HR+HD;

figure(1)
subplot(2,2,2)
bar(tline(1:1/dt:sizerealcc), realHos);
hold on
plot(tline,Hos,'-.','linewidth',3)
legend('Rep. Hos','Est. Hos','Location','NorthWest')
ylabel('Number of people','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)

[maxh,i]=max(Hos);
maxdate=datestr(datenum(datei)+floor(i*dt));
xajuste=datenum(maxdate);
xla=xline(xajuste,'-.g', maxdate,'linewidth',3);
xla.LabelVerticalAlignment = 'bottom';

set( get( get( xla, 'Annotation'), 'LegendInformation' ), 'IconDisplayStyle', 'off' );
legend show

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% QUARANTINE %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
Q = table2array(Tstates(:,8));

figure(1)
subplot(2,2,3)
bar(tline(1:1/dt:sizerealcc), realQ);
hold on
plot(tline,Q,'-.','linewidth',3)
legend('Rep. Q','Est. Q','Location','NorthWest')
ylabel('Number of people','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% RECOVERED %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
Rec=table2array(Tstates(:,9));

figure(1)
subplot(2,2,4)
bar(tline(1:1/dt:sizerealcc), realR);
hold on
plot(tline,Rec,'-.','linewidth',3)
legend('Rep. Recovered','Est. Recovered','Location','NorthWest')
ylabel('Number of people','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%% FIGURE 2   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% New Cases  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
CCNew = table2array(Tnewin(:,8));

realccNew(1)=0.0;
for i=2:length(realcc)
    realccNew(i)=max(realcc(i)- realcc(i-1),0);
end

figure(2)
subplot(2,2,1)
bar(tline(1:1/dt:sizerealcc), realccNew);
hold on
plot(tline(1:end-1),CCNew(1:end-1),'-','linewidth',3)
ylabel('Number of people/day','fontsize',12)
legend('Rep. New Cases','Est. New Cases','Location','NorthEast')
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)

[maxC,i]=max(CCNew);
maxdate=datestr(datenum(datei)+(i-1)*dt);%*dtfloor(i)
xajuste=datenum(maxdate);
xla=xline(xajuste,'-.g', maxdate,'linewidth',3);
xla.LabelVerticalAlignment = 'bottom';

set( get( get( xla, 'Annotation'), 'LegendInformation' ), 'IconDisplayStyle', 'off' );
legend show

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% PICO New Deaths %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
CDNew = table2array(Tnewin(:,10));

realcdNew(1)=0.0;
for i=2:length(realcd)
    realcdNew(i)=max(realcd(i)- realcd(i-1),0);
end

figure(2)
subplot(2,2,2)
bar(tline(1:1/dt:sizerealcc), realcdNew);
hold on
plot(tline(1:end-1),CDNew(1:end-1),'-','linewidth',3)
ylabel('Number of people/day','fontsize',12)
legend('Rep. New Deaths','Est. New Deaths','Location','NorthEast')
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)

[maxC,i]=max(CDNew);
maxdate=datestr(datenum(datei)+(i-1)*dt);%*dtfloor(
xajuste=datenum(maxdate);
xla=xline(xajuste,'-.g', maxdate,'linewidth',3);
xla.LabelVerticalAlignment = 'bottom';

set( get( get( xla, 'Annotation'), 'LegendInformation' ), 'IconDisplayStyle', 'off' );
legend show

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Re %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
Re = table2array(TRe(:,2));
ReE= table2array(TRe(:,3));
ReI= table2array(TRe(:,4));
ReIu=table2array(TRe(:,5));
ReHR=table2array(TRe(:,6));
ReHD=table2array(TRe(:,7));

figure(2)
subplot(2,2,4)

for jend=1:length(Re)
    if (Re(jend)==0)
        break;
    end
end

plot(tline(1:jend-1),Re(1:jend-1),'-b','linewidth',3)%time(1:end)
hold on
plot(tline(1:jend-1),ReE(1:jend-1),'--','linewidth',3)
plot(tline(1:jend-1),ReI(1:jend-1),'--','linewidth',3)
plot(tline(1:jend-1),ReIu(1:jend-1),'--','linewidth',3)
plot(tline(1:jend-1),ReHR(1:jend-1),'--','linewidth',3)
plot(tline(1:jend-1),ReHD(1:jend-1),'--','linewidth',3)
legend('Est. Re','Est. Re_E','Est. Re_I','Est. Re_Iu','Est. Re_{HR}','Est. Re_{HD}','Location','NorthEast')
ylabel('Re','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)
ylim([0.0,max(Re)])

for i=1:length(Re)
    if (Re(i)<1)
        break;
    end
end

datelt1=datestr(datenum(datei)+i*dt);
xajuste=datenum(datelt1);
xla=xline(xajuste,'-.k', datelt1,'linewidth',2);
xla.LabelVerticalAlignment = 'top';

set( get( get( xla, 'Annotation'), 'LegendInformation' ), 'IconDisplayStyle', 'off' );
legend show

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% CONTROL MEASURES %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
cmsocial= table2array(Tfeatures(:,13));
cmsanit = table2array(Tfeatures(:,14));
cmdates = table2array(Tcm(:,1));

figure(2)
subplot(2,2,3)
plot(tline(1:end),cmsocial(1:2:end),'-b','linewidth',3)
legend('Est. social distancing measures','Location','NorthEast')
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

for i=2:length(cmdates)
    datel=datestr(cmdates(i));
    xajuste=datenum(datel);
    xla=xline(xajuste,'-.k', datel,'linewidth',2);
    xla.LabelVerticalAlignment = 'bottom';

    set( get( get( xla, 'Annotation'), 'LegendInformation' ), 'IconDisplayStyle', 'off' );
    legend show
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%% FIGURA 3   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% CUMULATIVE TOTAL CASES %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

CCt = table2array(Tcumulat(:,6)); 
CCu = table2array(Tcumulat(:,5)); 

figure(3)
subplot(2,2,1)
plot((tline(1:1/dt:sizerealcc)),realcc,'linewidth',7,'markersize',5, 'color', [0.75 0.75 0.75])
hold on
plot(tline,CC,'--b','linewidth',3)
plot(tline,CCu,'-.','linewidth',3)
plot(tline,CCt,':','linewidth',3)
legend('Rep. Detected Cases','Est. Detected Cases','Est. Undetected Cases','Est. Total Cases (Detected+Undetected)','Location','NorthWest')
ylabel('Cumulative number of people','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;
xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% THETA %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

theta = table2array(Tfeatures(:,17));

figure(3)
subplot(2,2,2)
plot(tline(1:end),theta(1:2:end),'-','linewidth',3)
legend('Estimated \theta','Location','NorthWest')
ylabel('\theta','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% OMEGA %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

omega = table2array(Tfeatures(:,15));

figure(3)
subplot(2,2,3)
plot(tline(1:end),omega(1:2:end),'-','linewidth',3)
legend('\omega','Location','NorthWest')
ylabel('\omega','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% FUNCTION p %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

p = table2array(Tfeatures(:,18));

figure(3)
subplot(2,2,4)
plot(tline(1:end),p(1:2:end),'-','linewidth',3)
legend('Estimated p','Location','NorthWest')
ylabel('p','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
xlim([min(tline),max(tline)])
xtickangle(45)
