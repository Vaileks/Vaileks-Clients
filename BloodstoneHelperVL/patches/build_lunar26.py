"""Build the Lunar variant from the already restricted native 26.3 archive."""
from pathlib import Path
import argparse,json,os,subprocess,zipfile
root=Path(__file__).resolve().parent
parser=argparse.ArgumentParser()
parser.add_argument('--input',required=True,type=Path)
parser.add_argument('--output',required=True,type=Path)
args=parser.parse_args()
classpath=(root/'asm-path.txt').read_text().strip()
helpers=root/'lunar26/build/classes/java/main'
if not helpers.is_dir():raise SystemExit('Compile patches and lunar26 helper projects before packaging.')
args.output.parent.mkdir(parents=True,exist_ok=True)
subprocess.run([str(Path(os.environ['JAVA_HOME'])/'bin/java'),'-cp',classpath,'MakeLunar26',str(args.input),str(args.output),str(helpers)],check=True)
temp=args.output.with_suffix('.metadata.jar')
with zipfile.ZipFile(args.output) as src,zipfile.ZipFile(temp,'w',zipfile.ZIP_DEFLATED) as dst:
 for entry in src.infolist():
  content=src.read(entry.filename)
  if entry.filename=='fabric.mod.json':
   metadata=json.loads(content)
   assert metadata['depends']['minecraft']=='26.3'
   metadata['version']='1.3.8+lunarclient'
   metadata.setdefault('custom',{})['bloodhelper:distribution']='lunarclient'
   content=(json.dumps(metadata,ensure_ascii=False,indent=2)+'\n').encode()
  dst.writestr(entry,content)
temp.replace(args.output)
distribution=args.output.with_suffix('.distribution.jar')
subprocess.run([str(Path(os.environ['JAVA_HOME'])/'bin/java'),'-cp',classpath,'DistributionLinks',str(args.output),str(distribution),'true'],check=True)
distribution.replace(args.output)
