import os;

maxStep = 7

dir = "C:\\Users\\ZengC\\PycharmProjects\\WeatherPreProcess\\data";
for (root,dirs,files) in os.walk(dir,False) :
    Size = 0
    resf = open("test.txt", "w")
    for filename in files :
        path = os.path.join(root,filename);
        f = open(path,"r",encoding="utf8")

        lines = f.readlines();
        lists = []
        for line in lines:
            #print(line.rstrip());
            lists.append(line.rstrip())
        #print(lists)
        for i in range(len(lists)):
            if lists[i].find('晴')!=-1:
                lists[i] = '0'
            elif lists[i].find('阴')!=-1 or lists[i].find('云')!=-1:
                lists[i] = '1'
            elif lists[i].find('雨')!=-1:
                lists[i] = '2'
            elif lists[i].find('雪')!=-1:
                lists[i] = '3'
            else:
                lists[i] = '-1'
        reslists = []
        for i in range(len(lists)-maxStep+1):
            newlists = lists[i:i+maxStep]
            if '-1' in newlists:
                pass#print(newlists)
            else:
                reslists.append(newlists)
                Size += 1
                strs = " ".join(newlists)
                resf.writelines(strs+'\n')
        f.close()

        print(len(reslists))
    print(Size)
    resf.close()
