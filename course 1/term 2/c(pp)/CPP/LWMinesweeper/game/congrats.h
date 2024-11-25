#ifndef CONGRATS_H
#define CONGRATS_H
#include <QDialog>
#include <QLabel>
class Congrats : public QDialog
{
	Q_OBJECT
	QPushButton *m_btn;
	QLabel *m_text;

  public:
	Congrats(QString, QWidget *parent = nullptr);
};

#endif	  // CONGRATS_H
