#ifndef WIDGET_ALIGN_WRAPPER_H
#define WIDGET_ALIGN_WRAPPER_H
#include <QBoxLayout>
#include <QWidget>
class WidgetAlignWrapper : public QWidget
{
	Q_OBJECT

	QWidget *m_item;

  public:
	void set_widget(QWidget *);
	WidgetAlignWrapper(QWidget *, QWidget *parent = nullptr);
};

#endif	  // WIDGET_ALIGN_WRAPPER_H
