#ifndef FIELD_H
#define FIELD_H
#include "cell.h"
#include "counter.h"

#include <QGridLayout>
#include <QSettings>
#include <QWidget>

class Field : public QWidget
{
	Q_OBJECT
	unsigned int m_released = 0;
	unsigned int m_width;
	unsigned int m_height;
	unsigned int m_mined;
	unsigned int m_flag_count = 0;

	unsigned int m_settings_mines = 0;
	unsigned int m_settings_clicks = 0;
	bool m_control_swapped = false;
	bool m_dbg = false;

	bool m_dbg_show = false;
	QList< QList< Cell * > > m_cells;

	QSettings m_settings;
	Counter *m_counter;
	QPushButton *m_control_swap_btn;
	QPushButton *m_dbg_btn;
	void makeInactive();
	void reverseControl();
	void initGUI();
	bool propGuard(QString value);
	void showMines();

  protected:
	void resizeEvent(QResizeEvent *e) override;

  public:
	void loadSettings();
	void initGame(specs);
	void defineNeighbours();
	void settingsErrHandler();

	Field(specs = { 10, 10, 10 }, bool dbg = false, QWidget *parent = 0);

  signals:
	void onDefeat();
	void onWin();
	void onSettingsErr();

  public slots:
	void dbgHandler();
	void propGuardMines();
	void propGuardClick();
	void leftHandToggle();
	void flagged();
	void questioned();
	void firstClick(const position);
	void boomHandler(const position);
	void releaseCount();
};

#endif	  // FIELD_H
