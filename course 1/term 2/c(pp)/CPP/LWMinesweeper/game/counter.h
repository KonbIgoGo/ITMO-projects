#ifndef COUNTER_H
#define COUNTER_H
#include <QLCDNumber>
#include <QWidget>

class Counter : public QLCDNumber
{
	Q_OBJECT
	int m_mines = 0;
	int m_flags = 0;

	const QString m_style =
		"Counter"
		"{"
		"border: 5px solid rgb(64, 64, 64);"
		"background-color: rgb(143, 143, 143);"
		"border-radius: 1px;"
		"color: rgb(97, 1, 1);"
		"}";

  protected:
	QSize sizeHint() const override;

  public:
	void set_mines(int m_amount);
	void set_flags(int f_amount);

	Counter(QWidget *parent = nullptr);
};

#endif	  // COUNTER_H
