import os
saveTo='/home/sabita/Desktop/owlFiles/source/'
fname='namesForSource.txt'
files = 'eml2owl.py '
with open(fname) as f:
    content = [x.strip('\n') for x in f.readlines()]

for c in content:
    print c
    destination1=str(c.strip())+'.owl'
    destination=saveTo+destination1.replace('/','ZZZZ')
    command = 'python '+files+'  '+ str(c.strip())+'  '+destination
    print '\n',command
    os.system(command) 
	
