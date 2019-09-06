function extend(a, b){
    for(var key in b)
        if(b.hasOwnProperty(key))
            a[key] = b[key];
    return a;
}


var afinn = extend(afinn_en, afinn_emoticon);


function tokenize(input) {
  // convert negative contractions into negate_<word>
  return $.map(input.replace('.', '')
    .replace('/ {2,}/', ' ')
    .replace(/[.,\/#!$%\^&\*;:{}=_`~()]/g, '')
    .toLowerCase()
    .replace(/\w+['’]t\s+(a\s+)?(.*?)/g, 'negate_$2')
	.replace(/(^|\W)http(\w+)/g,'')
    .split(' '), $.trim);
}


function cleanPhrase(input) {
  // convert negative contractions into negate_<word>
  input = input.trim();
  return input.replace('.', '')
    .replace('/ {2,}/', ' ')
    .replace(/[.,\/#!$%\^&\*;:{}=_`~()@"]/g, '')
    .toLowerCase()
    .replace(/\w+['’]t\s+(a\s+)?(.*?)/g, 'negate_$2')
	.replace(/(^|\W)http(\w+)/g,'')
	.replace(/\n/g,'')
	.replace(/\r/g,'');
    
}

function sentiment(phrase) {

  var cleaned_phrase = cleanPhrase(phrase);
  var tokens = cleaned_phrase.split(' '),
    score = 0,
    words = [],
    positive = [],
    negative = [];

  // Iterate over tokens
  var len = tokens.length;
  while (len--) {
    var obj = tokens[len];
    var negate = obj.startsWith('negate_');

    if (negate) obj = obj.slice("negate_".length);

    if (!afinn.hasOwnProperty(obj)) continue;

    var item = afinn[obj];

    words.push(obj);
    if (negate) item = item * -1.0;
    if (item > 0) positive.push(obj);
    if (item < 0) negative.push(obj);

    score += item;
  }

  var verdict = score == 0 ? "NEUTRAL" : score < 0 ? "NEGATIVE" : "POSITIVE";

  var result = {
    verdict: verdict,
    score: score,
    comparative: score / tokens.length,
	no_of_positive_words : positive.length,
	no_of_negative_words : negative.length,
    positive: [positive],
    negative: [negative],
	cleaned_phrase: cleaned_phrase
  };

  return JSON.stringify(result);
}