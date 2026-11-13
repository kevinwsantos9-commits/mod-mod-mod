import re,os,sys
roots=['src/main/java','src/client/java']
files=[]
classes_by_pkg={}
for r in roots:
    for d,_,fs in os.walk(r):
        for f in fs:
            if f.endswith('.java'):
                p=os.path.join(d,f); files.append(p)
                s=open(p).read()
                m=re.search(r'^package\s+([\w.]+);',s,re.M)
                classes_by_pkg.setdefault(m.group(1),set()).add(f[:-5])
JAVA_LANG=set("String Math Object Integer Long Float Double Boolean Override Exception Runnable Iterable Number Character Byte Short Void Class System Thread Enum Record Deprecated SuppressWarnings FunctionalInterface IllegalStateException IllegalArgumentException RuntimeException Comparable StringBuilder Error Throwable".split())
def strip(s):
    s=re.sub(r'/\*.*?\*/','',s,flags=re.S)
    s=re.sub(r'//[^\n]*','',s)
    s=re.sub(r'"(\\.|[^"\\])*"','""',s)
    s=re.sub(r"'(\\.|[^'\\])'","''",s)
    return s
bad=0
for p in sorted(files):
    raw=open(p).read(); s=strip(raw)
    pkg=re.search(r'^package\s+([\w.]+);',raw,re.M).group(1)
    for a,b in ['{}','()','[]']:
        if s.count(a)!=s.count(b):
            print(f"{p}: desbalanceado {a}{b} {s.count(a)} vs {s.count(b)}"); bad+=1
    imports={}
    for m in re.finditer(r'^import\s+(static\s+)?([\w.]+)\.(\w+|\*);',raw,re.M):
        imports[m.group(3)]=m.group(2)
    body=re.sub(r'^(import|package)[^\n]*\n','',s,flags=re.M)
    declared=set(re.findall(r'\b(?:class|interface|enum|record)\s+(\w+)',body))
    declared|=set(re.findall(r'<\s*(\w)\s*[,>]',body))
    used=set(re.findall(r'\b([A-Z][A-Za-z0-9]*)\b',body))
    for u in sorted(used):
        if u in imports or u in JAVA_LANG or u in declared: continue
        if u in classes_by_pkg.get(pkg,()): continue
        if u.isupper() or re.fullmatch(r'[A-Z][A-Z0-9_]+',u): continue  # constantes
        # constante/campo qualificado por classe importada nao entra aqui; reporta o resto
        print(f"{p}: sem import? {u}"); bad+=1
    for name,pk in imports.items():
        if name!='*' and not re.search(r'\b'+name+r'\b',body):
            print(f"{p}: import nao usado {pk}.{name}")
print("problemas:",bad)
