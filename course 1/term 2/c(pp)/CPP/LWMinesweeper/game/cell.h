#ifndef CELL_H
#define CELL_H
#include "types.h"

#include <QMouseEvent>
#include <QPushButton>
#include <QSet>
#include <QSettings>
#include <QWidget>

class Cell : public QPushButton
{
	Q_OBJECT
	const position cord;

	QSettings settings;

	QSet< Cell * > m_neighbours;
	QSet< Cell * > m_mined_neighbours;

	bool dbg = false;
	bool controlReverse = false;
	bool mined = false, fClicked = false, released = false, flagged = false, active = true, questioned = false;

	const QString m_questioned_style =
		"Cell"
		"{"
		"border: 1px solid darkgray;"
		"background-color: rgb(0, 174, 194);"
		"border-radius: 1px;"
		"}";

	const QString m_flagged_style =
		"Cell"
		"{"
		"border: 1px solid darkgray;"
		"background-color: rgb(0, 194, 58);"
		"border-radius: 1px;"
		"}";

	const QString m_unreleased_style =
		"Cell"
		"{"
		"border: 1px solid darkgray;"
		"background-color: gray;"
		"border-radius: 1px;"
		"}";
	const QString m_released_style =
		"Cell"
		"{"
		"border: 1px solid lightgray;"
		"background-color: white;"
		"}";
	;
	const QString m_numbered_style =
		"Cell"
		"{"
		"color: %1;"
		"font-weight: bold;"
		"border: 1px solid lightgray;"
		"}";
	const QString m_bomb_debug_style =
		"Cell"
		"{"
		"border: 1px solid darkgray;"
		"background-color: rgb(107, 2, 17);"
		"border-radius: 1px;"
		"}";
	const QString m_boom_style =
		"Cell"
		"{"
		"border: 1px solid darkgray;"
		"background-color: red;"
		"border-radius: 1px;"
		"}";

	void midClickHandler();
	void saveSettings(QString = "*", bool = true);

  protected:
	QSize sizeHint() const override;
	int heightForWidth(int w) const override;

  public:
	void toggleDbg();
	void setFClick();
	void reverseControl();
	void setFirstClicked();
	void addNeighbour(Cell *);
	void setupMine();
	void setDefeatStyle();
	bool isMined();
	bool isReleased();
	void setActive(bool);
	bool isFirstClicked();
	Cell(position cord, QWidget *parent = nullptr);

  signals:
	void rightClicked();
	void firstClicked(position);
	void onDefeat(const position);
	void isFlagged();
	void isQuestioned();
	void onReleased();

  public slots:
	void mousePressEvent(QMouseEvent *) override;
	void toggleFlag();
	void release();
	void firstClick();
};

#endif	  // CELL_H
