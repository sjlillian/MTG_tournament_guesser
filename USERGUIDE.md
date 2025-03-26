# MTG Deck Prediction AI - Quick-Start Guide
## 1. Installation

To use this application, first clone the repository from GitHub:

`git clone https://github.com/sjlillian/MTG_tournament_guesser.git`

`cd .\commander-ml\`

***Ensure you have Java 17+ and Maven installed.*** 

To build the project, run:

`mvn clean install`

## 2. Running the Application
To execute the program, simply run:

`java -jar target\commander-ml-1.jar`

This will launch the prediction system.

## 3. Providing User Data 
### CSV File Format
Users must provide tournament deck data in CSV format. The file should follow this format:

> tournament_id,deck_id,commander,partner,author,card_name
1001,1,"Kediss, Emberclaw Familiar","Malcolm, Keen-Eyed Navigator",By Joshua Smith,Glint-Horn Buccaneer
1001,1,"Kediss, Emberclaw Familiar","Malcolm, Keen-Eyed Navigator",By Joshua Smith,Spellseeker
...

- **tournament_id**: Unique ID of the tournament.

- **deck_id**: Unique ID of the deck within the tournament.

- **commander**: Primary commander of the deck.

- **partner**: Secondary commander (if applicable), otherwise "N/A".

- **author**: Name of the deck creator. *NOT needed or used in the program, just an expected column in the data!*

- **card_name**: Name of each card in the deck. *Double cards (Adventure, Transform, etc.) should contain either sides name, NOT both!*

### Loading the CSV File
To use your data, place the CSV file in the `data/` directory and specify its location when prompted.

## 4. Interpreting the Output
After running, the program will output predictions like this:

```
Final User Tournament Predictions:
Deck: Thrasios, Triton Hero & Tymna the Weaver | Avg Rank%: 0.567 | Most Common Bracket: 4 | Score: 0.457 | Confidence: 0.998
Deck: Kinnan, Bonder Prodigy | Avg Rank%: 0.553 | Most Common Bracket: 4 | Score: 0.447 | Confidence: 0.997
...
```

- **Avg Rank%**: Predicted rank percentile (closer to 1.0 means a higher chance of winning).

- **Most Common Bracket**: Most frequently predicted competitive bracket.

- **Score**: Composite ranking score based on regression and classification models.

- **Confidence**: How certain the model is about its prediction (closer to 1.0 is more confident).

The decks will be in order of who most likely to win.

## 5. Using the Visualizer
To better understand the data and predictions, the Visualizer provides several interactive plots.

### Navigation
The application features multiple tabs:

1. **Feature vs Rank%** - Scatterplots comparing features with win probability.

2. **Feature vs Rank Bracket** - Scatterplots for classification analysis.

3. **Commander Popularity** - Displays how common certain commanders are.

4. **Deck Feature Breakdown** - Examines deck compositions and key characteristics.

5. **Feature Correlation Matrix** - Heatmaps showing feature relationships.

## 6. Troubleshooting & FAQs
***Q: My CSV file isn't loading properly.***

Ensure the file follows the correct comma-separated format.

Make sure card names and commanders are properly enclosed in double quotes if they contain commas.

***Q: The predictions seem off—how can I improve accuracy?***

Try adding more tournament data to the training dataset.

Experiment with different feature selection techniques to improve deck classification.

***Q: How do I update the application?***

Pull the latest changes from GitHub:

`git pull origin main`

***Q: The application is saying "Card not found: ~~~". What do I do?***

If a card is not found, the first thing to check is the `refined_cards.csv` file. It could be as simple as a missing capitolization or appostrophe. If that is the case, update your csv file to match what is in `refined_cards.csv`. If the database file does NOT contain your card, create a new Issue for it in the github repository. Put the name of your card as well as all the information needed to fill out the entry in `refined_cards.csv`, as well as a link to a website to prove that it is a legit card.

**Note: If your card is a flavor card (i.e. Affrican Swallow // Eurpoean Swallow), then it will not show up in `refined_cards.csv`!**