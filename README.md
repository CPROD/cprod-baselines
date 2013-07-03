cprod-baselines
===============

Java code for the baseline solutions for the CPROD1 contest
<<<<<<< HEAD

===============

Baseline 1: <description>
Baseline 2: <description>

Most actions done with the Baseline class. This program needs as an input a config file in which all actions are defined.
	java -Xmx4500m -cp "./bin;./lib/*;./lib/mallet" baseline/Baseline <config.file>

The config file is a json file in which you can specify the following parameters (paramters with an (*) are mandatory)
parameter name	description						Default value			type
train					whether to train the model							true							boolean
test					whether to test the model							true							boolean
filter					whether to filter the results						false							boolean
link					whether to link to the catalog						false							boolean
useDictionary			whether to use dictionary attributes for the crf	true							boolean
usePatternFeatures		whether to use pattern features for the crf			false							boolean
useTokenFeature			whether to use token features for the crf			false							boolean
evaluate				whether to evaluate the result against the ground truth	false						boolean
tokenWindow				token window for the crf							1								integer
home_path				directory where all data files can be found			""								String
training_text_file		name of the training text file						"training-annotated-text.json"	String
training_annotations_file name of the csv file with traning annotations		"training-disambiguated-product-mentions.120725.csv"	String
test_text_file			name of the test text file							"evaluation-text.json"			String
regex_feature_file		name of the file with regex features for CRF		"regexfeatures_test.json"		String
dictionary_file			name of the dictionary file							"dictionary.dat"				String
annotation_result_file  name of the file where the results should be written $model + "_results.csv"		String
ground_truth_annotations_file	name of the file with ground truth annotations	"evaluation-disambiguated-product-mentions.120725.csv"	String
index_file				name of the file with index of the product catalog	"index.txt"						String
product_dictionary_file name of file with additionary product terms			null							String
filters					list of filters to use								empty array						one or more of {BLACKLIST, BRANDNAME, FIRST_TOKEN, MENTION_LENGTH, PROCEEDINGWORDS_ALLOWED, PROCEEDINGWORDS_BLACKLIST1, PROCEEDINGWORDS_BLACKLIST2,SPECIAL_CHAR, MILD, STRICT}
model					Type of model to use								BASELINE1						{BASELINE1, BASELINE2, BASELINE3, RULEBASE}
model_name				Name of the model that corresponds to this config		baseline1						String


Other programs:
	java -cp "./bin;./lib/*;./lib/mallet" baseline/Ensemble <results.file1> <results.file2> <ground.truth.file1> <output.file>
Merges two different results files, giving priority to the annotations found in the first file. 

	java -cp "./bin;./lib/*;./lib/mallet" baseline/PrintAnnotations <text.file> <result.file>
Prints the annotations with the mention phrases included to standard output.

	java -cp "./bin;./lib/*;./lib/mallet" baseline/CatalogHandler 
	args:
	-catalog-file <
=======
>>>>>>> dc6dd16f034ed38770c31fdf6fa8f092fc742466
