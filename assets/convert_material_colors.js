fs=require('fs')

c=fs.readFileSync('colors.xml')
s=c.toString().split('\n').filter(l => l.indexOf('name="material')!==-1).map(l => l.trim())

function convertColorXmlToKotlin(xmlString) {
  const regex = /<color name="(.+?)">#([0-9A-Fa-f]+)<\/color>/;
  const match = xmlString.match(regex);

  if (!match) return null;

  const name = match[1];
  const hex = match[2];

  // Convert name like "material_deep_orange_500" to "DeepOrange500"
  const nameParts = name.split('_').slice(1); // remove "material"
  const kotlinName = nameParts
    .map((part, i) => i === nameParts.length - 1 ? part : part.charAt(0).toUpperCase() + part.slice(1))
    .join('');

  return `val ${kotlinName} = Color(0x${hex.toUpperCase()}FF)`;
}

d=s.map(convertColorXmlToKotlin)
console.log(d)
fs.writeFileSync('/tmp/kotlin', d.join('\n'))
