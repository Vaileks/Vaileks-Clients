"""Offline-only server-pack fixture; uses Minecraft's own models, no server art."""
from pathlib import Path
import json
import zipfile

dest = Path(__file__).resolve().parent.parent / 'run/fixture-pack.zip'
dest.parent.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(dest, 'w', zipfile.ZIP_DEFLATED) as output:
    output.writestr('pack.mcmeta', json.dumps({'pack': {'pack_format': 75, 'min_format': 75,
        'max_format': 75, 'description': 'Pyz1k offline integration fixture'}}))
    for item in ['diamond_sword', 'netherite_sword', 'bow', 'crossbow', 'totem_of_undying', 'mace']:
        output.writestr('assets/minecraft/items/' + item + '.json', json.dumps({'model': {
            'type': 'minecraft:range_dispatch', 'property': 'minecraft:custom_model_data',
            'entries': [
                {'threshold': 1, 'model': {'type': 'minecraft:model', 'model': 'minecraft:item/golden_sword'}},
                {'threshold': 2, 'model': {'type': 'minecraft:model', 'model': 'minecraft:item/diamond_sword'}}],
            'fallback': {'type': 'minecraft:model', 'model': 'minecraft:item/' + item}}}))
print(dest)
with zipfile.ZipFile(dest, 'a', zipfile.ZIP_DEFLATED) as output:
    for form,model in [('sword','netherite_sword'),('block','netherite_scrap'),('mace','mace'),('harpoon','bow')]:
        output.writestr('assets/bloodstone/items/tools/bloodsword/default/'+form+'.json',json.dumps({'model':{'type':'minecraft:model','model':'minecraft:item/'+model}}))

with zipfile.ZipFile(dest, 'a', zipfile.ZIP_DEFLATED) as output:
    for identifier,model in [
        ('astrum:smp/totem_of_undying/creeper','totem_of_undying'),
        ('minecraft:wither_totem','diamond'),
        ('test:swords/diamond_sword/ruby','diamond_sword'),
        ('test:swords/netherite_sword/obsidian','netherite_sword'),
        ('test:icons/skills/totem','totem_of_undying')]:
        namespace,path=identifier.split(':',1)
        output.writestr('assets/'+namespace+'/items/'+path+'.json',json.dumps({'model':{'type':'minecraft:model','model':'minecraft:item/'+model}}))
    output.writestr('assets/test/items/totems/royal.json',json.dumps({'model':{'type':'minecraft:composite','models':[{'type':'minecraft:model','model':'minecraft:item/totem_of_undying'},{'type':'minecraft:model','model':'minecraft:item/gold_ingot'}]}}))
