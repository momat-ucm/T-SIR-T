close all;clear all;clc;
currentFolder = pwd;

% Source REPORTED DATA:
T=readtable(strcat(currentFolder,'\Scenarios\Italy\2\timeseries.csv'));
dates  = table2array(T(:,1));
realcc = table2array(T(:,2));
realcd = table2array(T(:,3));
realHos= table2array(T(:,4));
realQ  = table2array(T(:,5));
realR  = table2array(T(:,6));
Tcm=readtable(strcat(currentFolder,'\Scenarios\Italy\2\controlmeasures.csv'));

for i=1:4
    basedir{i} = strcat(currentFolder,'\Output\Italy\',num2str(i+1));
    Tstates{i} = readtable(strcat(basedir{i},'\states.csv'));
    TRe{i}     = readtable(strcat(basedir{i},'\Re.csv'));
    Tnewin{i}  = readtable(strcat(basedir{i},'\newin.csv'));
    Tfeatures{i}=readtable(strcat(basedir{i},'\features.csv'));
    Tcumulat{i} =readtable(strcat(basedir{i},'\cumulative.csv'));
    Tcauses{i}  =readtable(strcat(basedir{i},'\causesOfInfection.csv'));
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
%% CC and CD   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
figure(1)
subplot(1,2,1)
plot((tline(1:1/dt:sizerealcc)),realcc,'linewidth',7,'markersize',5, 'color', [0.75 0.75 0.75])
hold on
for i=1:4
    CC{i} = table2array(Tcumulat{i}(:,2));
    plot(tline,CC{i},plotlines{i},'linewidth',3)
end
title('Cumulative Cases')
legend('Rep.','Est. without 2nd variant without vaccines','Est. with 2nd variant without vaccines','Est. without 2nd variant with vaccines','Est. with 2nd variant with vaccines','Location','NorthWest')
ylabel('Cumulative number of people','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;
xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
%xlim([min(tline),max(tline)])
xlim([min(tline),tline(tline(:)==datenum('30-Jul-2021'))])
xtickangle(45)

figure(1)
subplot(1,2,2)
plot((tline(1:1/dt:sizerealcc)),realcd,'linewidth',7,'markersize',5, 'color', [0.75 0.75 0.75])
hold on
for i=1:4
    CD{i} = table2array(Tcumulat{i}(:,3));
    plot(tline,CD{i},plotlines{i},'linewidth',3)
end
title('Cumulative Deaths')
legend('Rep.','Est. without 2nd variant without vaccines','Est. with 2nd variant without vaccines','Est. without 2nd variant with vaccines','Est. with 2nd variant with vaccines','Location','NorthWest')
ylabel('Cumulative number of people','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;
xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
%xlim([min(tline),max(tline)])
xlim([min(tline),tline(tline(:)==datenum('30-Jul-2021'))])
xtickangle(45)

hfig = figure(1);
pos = get(hfig,'position');
set(hfig,'position',pos.*[.5 1 2 1]);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%% FIGURE 2   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% New Cases  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

realccNew(1)=0.0;
for i=2:length(realcc)
    realccNew(i)=max(realcc(i)- realcc(i-1),0);
end

figure(2)
subplot(1,2,1)
bar(tline(1:1/dt:sizerealcc), realccNew);
hold on
for i=1:4
    CCNew{i} = table2array(Tnewin{i}(:,8));
    plot(tline(1:end-1),CCNew{i}(1:end-1),plotlines{i},'linewidth',3)
end
title('New cases')
legend('Rep.','Est. without 2nd variant without vaccines','Est. with 2nd variant without vaccines','Est. without 2nd variant with vaccines','Est. with 2nd variant with vaccines','Location','NorthWest')
ylabel('Number of people/day','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
%xlim([min(tline),max(tline)])
xlim([min(tline),tline(tline(:)==datenum('30-Jul-2021'))])
xtickangle(45)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%      New Deaths %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

realcdNew(1)=0.0;
for i=2:length(realcd)
    realcdNew(i)=max(realcd(i)- realcd(i-1),0);
end

figure(2)
subplot(1,2,2)
bar(tline(1:1/dt:sizerealcc), realcdNew);
hold on
for i=1:4
    CDNew{i} = table2array(Tnewin{i}(:,10));
    plot(tline(1:end-1),CDNew{i}(1:end-1),plotlines{i},'linewidth',3)
end
title('New deaths')
legend('Rep.','Est. without 2nd variant without vaccines','Est. with 2nd variant without vaccines','Est. without 2nd variant with vaccines','Est. with 2nd variant with vaccines','Location','NorthWest')
ylabel('Number of people/day','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

xticks(tvec)
xticklabels(datestr(tvec,'dd mmm yyyy'))
%xlim([min(tline),max(tline)])
xlim([min(tline),tline(tline(:)==datenum('30-Jul-2021'))])
xtickangle(45)

hfig = figure(2);
pos = get(hfig,'position');
set(hfig,'position',pos.*[.5 1 2 1]);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%% FIGURE 3   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%      New Deaths %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
newD21 = table2array(Tnewin{2}(:,19)); 
newD22 = table2array(Tnewin{2}(:,28)); 

newD41 = table2array(Tnewin{4}(:,19)); 
newD42 = table2array(Tnewin{4}(:,28)); 

figure(3)
subplot(2,2,1)
hbar = bar(tline(1:1/dt:end-1),[newD21(1:1/dt:end-1) newD22(1:1/dt:end-1)],'stacked');
hbar(1).FaceColor = 'flat';
hbar(1).CData = coloresvar{1};
hbar(2).FaceColor = 'flat';
hbar(2).CData = coloresvar{2};
title('Without vaccines')
legend('New deaths reference variant', 'New deaths new variant','Location','NorthWest')
ylabel('Number of people','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;
tvec2=linspace(tline(tline(:)==datenum('01-Nov-2020')),tline(nmax),10);
%tvec21=linspace(tline(tline(:)==datenum('01-Nov-2020')),tline(tline(:)==datenum('30-Jul-2021')),10);
xlim([tline(tline(:)==datenum('01-Nov-2020')),tline(tline(:)==datenum('30-Jul-2021'))])
%xlim([tline(tline(:)==datenum('01-Nov-2020')),max(tline)])
xticks(tvec2)
xticklabels(datestr(tvec2,'dd mmm yyyy'))
xtickangle(45)

figure(3)
subplot(2,2,2)
hbar = bar(tline(1:1/dt:end-1),[newD41(1:1/dt:end-1) newD42(1:1/dt:end-1)],'stacked');
hbar(1).FaceColor = 'flat';
hbar(1).CData = coloresvar{3};
hbar(2).FaceColor = 'flat';
hbar(2).CData = coloresvar{4};
title('With vaccines')
legend('New deaths reference variant', 'New deaths new variant','Location','NorthWest')
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

for i=1:4
    Re{i} = table2array(TRe{i}(:,2));
end


RePond = table2array(TRe{2}(:,2));
Re21   = table2array(TRe{2}(:,3));
Re22   = table2array(TRe{2}(:,9));

figure(3)
subplot(2,2,3)
jini=0;
for jend=1:length(RePond)
    if (RePond(jend)==0)
        break;
    end
    if(Re22(jend)==0)
        jini=jini+1;
    end
end
plot(tline(1:jend-1),Re{1}(1:jend-1),'-','linewidth',4,'Color',colores{1})%time(1:end)
hold on
plot(tline(1:jend-1),RePond(1:jend-1),'--','linewidth',4,'Color',colores{2})%time(1:end)
plot(tline(1:jend-1),Re21(1:jend-1),'-.','linewidth',3,'Color',coloresvar{1})
plot(tline(jini+1:jend-1),Re22(jini+1:jend-1),':','linewidth',3,'Color',coloresvar{2})
legend('Without 2nd variant: Re','With 2nd variant: Re','With 2nd variant: Re^{(1)}','With 2nd variant: Re^{(2)}','Location','NorthWest')
title('Without vaccines')
ylabel('Re','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;

ylim([0.0,2.2])%max(max(Re21),max(RePond))
xlim([tline(tline(:)==datenum('01-Nov-2020')),tline(tline(:)==datenum('30-Jul-2021'))])
%xlim([tline(tline(:)==datenum('01-Nov-2020')),max(tline)])
xticks(tvec2)
xticklabels(datestr(tvec2,'dd mmm yyyy'))
xtickangle(45)

RePond4 = table2array(TRe{4}(:,2));
Re41   = table2array(TRe{4}(:,3));
Re42   = table2array(TRe{4}(:,9));

figure(3)
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

plot(tline(1:jend-1),Re{3}(1:jend-1),'-','linewidth',4,'Color',colores{3})%time(1:end)
hold on
plot(tline(1:jend-1),RePond4(1:jend-1),'--','linewidth',4,'Color',colores{4})%time(1:end)
plot(tline(1:jend-1),Re41(1:jend-1),'-.','linewidth',3,'Color',coloresvar{3})
plot(tline(jini+1:jend-1),Re42(jini+1:jend-1),':','linewidth',3,'Color',coloresvar{4})
legend('Without 2nd variant: Re','With 2nd variant: Re','With 2nd variant: Re^{(1)}','With 2nd variant: Re^{(2)}','Location','NorthWest')
title('With vaccines')
ylabel('Re','fontsize',12)
axis tight
yy=ylim;
xx=xlim;
grid on;
xlim([tline(tline(:)==datenum('01-Nov-2020')),max(tline)])
ylim([0.0,2.2])
xticks(tvec2)
xticklabels(datestr(tvec2,'dd mmm yyyy'))
xtickangle(45)
%ylim([0.0,max(max(Re41),max(RePond4))])

