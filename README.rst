Elasticsearch Word Decompounder Analysis Plugin
===============================================

This is an implementation of a word decompounder plugin for `Elasticsearch <http://github.com/elasticsearch/elasticsearch>`_.

This word decompounding token filter is complementing the `standard Elasticsearch compound word token filter <http://www.elasticsearch.org/guide/reference/index-modules/analysis/compound-word-tokenfilter.html>`_

Compounding several words into one word is a property not all languages share. Compounding is used in German, Scandinavian Languages, Finnish and Korean.

This code is a reworked implementation of the `Baseforms Tool <http://wortschatz.uni-leipzig.de/~cbiemann/software/toolbox/Baseforms%20Tool.htm>`_ found in the `ASV toolbox <http://wortschatz.uni-leipzig.de/~cbiemann/software/toolbox/index.htm>`_  of `Chris Biemann <http://asv.informatik.uni-leipzig.de/staff/Chris_Biemann>`_, Automatische Sprachverarbeitung of Leipzig University.

Lucene comes with two coumpound word token filters, a dictionary- and a hyphenation-based variant. Both of them have a disadvantage, they require loading a word list in memory before they run. This decompounder does not require word lists, it can process german language text out of the box. The decompounder uses prebuilt *Compact Patricia Tries* for efficient word segmentation provided by the ASV toolbox.


Installation
------------

The latest version is **1.3.0** (Oct 26, 2013)

Prerequisites::

  Elasticsearch 0.90+

=============  =========  =================  =============================================================
ES version     Plugin     Release date       Command
-------------  ---------  -----------------  -------------------------------------------------------------
0.90.2         1.2.0      Jul 17, 2013       ./bin/plugin --install decompound --url http://bit.ly/1bJCygc
0.90.5         1.2.1      Oct 14, 2013       ./bin/plugin --install decompound --url http://bit.ly/1cP4Gkt
0.90.5         **1.3.0**  Oct 26, 2013       ./bin/plugin --install decompound --url http://bit.ly/1dbOs2u
=============  =========  =================  =============================================================


Example
=======

In the mapping, us a token filter of type "decompound"::

  {
     "index":{
        "analysis":{
            "filter":{
                "decomp":{
                    "type" : "decompound"
                }
            },
            "tokenizer" : {
                "decomp" : {
                   "type" : "standard",
                   "filter" : [ "decomp" ]
                }
            }
        }
     }
  }

"Die Jahresfeier der Rechtsanwaltskanzleien auf dem Donaudampfschiff hat viel Ökosteuer gekostet" will be tokenized into 
"Die", "Die", "Jahresfeier", "Jahr", "feier", "der", "der", "Rechtsanwaltskanzleien", "Recht", "anwalt", "kanzlei", "auf", "auf", "dem",  "dem", "Donaudampfschiff", "Donau", "dampf", "schiff", "hat", "hat", "viel", "viel", "Ökosteuer", "Ökosteuer", "gekostet", "gekosten"

It is recommended to add the `Unique token filter <http://www.elasticsearch.org/guide/reference/index-modules/analysis/unique-tokenfilter.html>`_ to skip tokens that occur more than once.

Also the Lucene german normalization token filter is provided::

  {
    "index":{
        "analysis":{
            "filter":{
                "umlaut":{
                    "type":"german_normalize"
                }
            },
            "tokenizer" : {
                "umlaut" : {
                   "type":"standard",
                   "filter" : "umlaut"
                }            
            }
        }
    }
  }

The input "Ein schöner Tag in Köln im Café an der Straßenecke" will be tokenized into "Ein", "schoner", "Tag", "in", "Koln", "im", "Café", "an", "der", "Strassenecke".

Threshold
---------

The decomposing algorithm knows about a threshold when to assume words as decomposed successfully or not.
If the threshold is too low, words could silently disappear from being indexed. In this case, you have to adapt the
threshold so words do no longer disappear.

The default threshold value is 0.51. You can modify it in the settings::

  {
     "index":{
        "analysis":{
            "filter":{
                "decomp":{
                    "type" : "decompound",
                    "threshold" : 0.51
                }
            },
            "tokenizer" : {
                "decomp" : {
                   "type" : "standard",
                   "filter" : [ "decomp" ]
                }
            }
        }
     }
  }




References
==========

The Compact Patricia Trie data structure can be found in 

*Morrison, D.: Patricia - practical algorithm to retrieve information coded in alphanumeric. Journal of ACM, 1968, 15(4):514–534*

The compound splitter used for generating features for document classification is described in

*Witschel, F., Biemann, C.: Rigorous dimensionality reduction through linguistically motivated feature selection for text categorization. Proceedings of NODALIDA 2005, Joensuu, Finland*

The base form reduction step (for Norwegian) is described in

*Eiken, U.C., Liseth, A.T., Richter, M., Witschel, F. and Biemann, C.: Ord i Dag: Mining Norwegian Daily Newswire. Proceedings of FinTAL, Turku, 2006, Finland*


License
=======

Elasticsearch Word Decompounder Analysis Plugin

Copyright (C) 2012 Jörg Prante

Derived work of ASV toolbox http://asv.informatik.uni-leipzig.de/asv/methoden

Copyright (C) 2005 Abteilung Automatische Sprachverarbeitung, Institut für Informatik, Universität Leipzig

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.