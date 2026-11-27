"""Read only the six numeric Blood Sword fields needed by the client HUD."""
from pathlib import Path
import hashlib,json,re,os
ROOT=Path(__file__).resolve().parent.parent
PRESETS=Path(os.environ.get('BLOOD_PRESETS_DIR', 'presets'))
MODES=[('meetups','Митапы'),('uhc','УхкМитапы'),('v1_battle_royale','КастомКБ'),('very_fast_battleroyale','БыстраяКБ'),('smp','СМП/ФФА СМП')]
FIELDS=['mace_duration','transition_duration','cooldown','bow_duration','bow_cooldown','bow_pull_cooldown']
result=[]
for folder,label in MODES:
    path=PRESETS/folder/'uLegendary/bloodsword.yml'
    entry={'id':folder,'label':label,'serverOnly':folder=='smp','values':{}}
    if path.is_file():
        raw=path.read_bytes();text=raw.decode('utf-8')
        ability=text.split('\nability:\n',1)[1].split('\nlore-',1)[0]
        entry['sha256']=hashlib.sha256(raw).hexdigest()
        entry['source']=folder+'/uLegendary/bloodsword.yml'
        for field in FIELDS:
            match=re.search(r'^  '+field+r':\s*([0-9]+(?:\.[0-9]+)?)\s*$',ability,re.M)
            if not match:raise ValueError((folder,field))
            entry['values'][field]=float(match[1])
    elif folder=='smp':
        entry['source']='No bloodsword.yml in smp; server-only mode explicitly requested by user.'
    else:raise FileNotFoundError(path)
    result.append(entry)
(ROOT/'src/main/resources/bloodsword-presets.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(result,ensure_ascii=False,indent=2))
