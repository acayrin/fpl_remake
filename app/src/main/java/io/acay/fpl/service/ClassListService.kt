package io.acay.fpl.service

import io.acay.fpl.model.ClassF
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Random

class ClassListService {
    companion object {
        // dummy data
        // TODO implement API
        fun getClasses(): ArrayList<ClassF> {
            val classList = arrayListOf<ClassF>()

            for (i in 0..20) {
                classList.add(
                    ClassF(
                        i,
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(
                            System.currentTimeMillis() + Random().nextInt(24 * 3_600 * 14 * 1_000) - Random().nextInt(
                                24 * 3_600 * 14 * 1_000
                            )
                        ),
                        "Room ${i}",
                        "Location ${i}",
                        "Subject ID ${i}",
                        "Subject ${i}",
                        "Class ${i}",
                        "Teacher #${i}",
                        Random().nextInt(5),
                        "xx:xx",
                        "yy:yy",
                        null,
                        null
                    )
                )
            }

            classList.sortBy {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(it.date)
            }

            return classList
        }

        fun getClasses(res: (ArrayList<ClassF>) -> Unit) {
            res.invoke(getClasses())
        }
    }
}