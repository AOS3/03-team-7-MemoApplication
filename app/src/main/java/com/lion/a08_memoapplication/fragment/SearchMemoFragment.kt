package com.lion.a08_memoapplication.fragment

import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.lion.a08_memoapplication.MainActivity
import com.lion.a08_memoapplication.R
import com.lion.a08_memoapplication.databinding.DialogMemoPasswordBinding
import com.lion.a08_memoapplication.databinding.FragmentSearchMemoBinding
import com.lion.a08_memoapplication.databinding.RowMemoBinding
import com.lion.a08_memoapplication.databinding.RowText1Binding
import com.lion.a08_memoapplication.fragment.ShowMemoAllFragment.RecyclerShowMemoAdapter.ViewHolderMemoAdapter
import com.lion.a08_memoapplication.model.MemoModel
import com.lion.a08_memoapplication.repository.MemoRepository
import com.lion.a08_memoapplication.util.FragmentName
import com.lion.a08_memoapplication.util.MemoListName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SearchMemoFragment : Fragment() {

    lateinit var fragmentSearchMemoBinding: FragmentSearchMemoBinding
    lateinit var mainActivity: MainActivity

//    // 리사이클러 뷰 구성을 위한 임시 데이터
//    val tempData = Array(100){
//        "메모 ${it + 1}"
//    }

    // 리사클리어뷰 구성을 위한 리스트
    var memoList = mutableListOf<MemoModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        fragmentSearchMemoBinding = FragmentSearchMemoBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        // 툴바를 구현하는 메서드
        settingToolbarSearchMemo()
        // recyclerView를 구성하는 메서드
        settingRecyclerViewSearchMemo()
        // 입력 요소 설정 메서드를 호출한다.
        settingTextField()

        return fragmentSearchMemoBinding.root
    }

    // 툴바를 구현하는 메서드
    fun settingToolbarSearchMemo(){
        fragmentSearchMemoBinding.apply {
            toolbarSearchMemo.title = "메모 정보 검색"

            toolbarSearchMemo.setNavigationIcon(R.drawable.arrow_back_24px)
            toolbarSearchMemo.setNavigationOnClickListener {
                mainActivity.removeFragment(FragmentName.SEARCH_MEMO_FRAGMENT)
            }
        }
    }

    // recyclerView를 구성하는 메서드
    fun settingRecyclerViewSearchMemo(){
        fragmentSearchMemoBinding.apply {
            recyclerViewSearchMemo.adapter = RecyclerViewMemoSearchAdapter()
            recyclerViewSearchMemo.layoutManager = LinearLayoutManager(mainActivity)
            val deco = MaterialDividerItemDecoration(mainActivity, MaterialDividerItemDecoration.VERTICAL)
            recyclerViewSearchMemo.addItemDecoration(deco)
        }
    }

//    // Recyclerview의 어뎁터
//    inner class RecyclerViewMemoSearchAdapter : RecyclerView.Adapter<RecyclerViewMemoSearchAdapter.ViewHolderMemoSearch>(){
//        // ViewHolder
//        inner class ViewHolderMemoSearch(val rowMemoBinding: RowMemoBinding) : RecyclerView.ViewHolder(rowMemoBinding.root), OnClickListener{
//            override fun onClick(v: View?) {
//
//                // 메모 정보를 보는 화면으로 이동
//                val dataBundle = Bundle()
//                dataBundle.putInt("memoIdx", memoList[adapterPosition].memoIdx)
//                mainActivity.replaceFragment(FragmentName.READ_MEMO_FRAGMENT,true,true,dataBundle)
//
//            }
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMemoSearch {
//            val rowMemoBinding = RowMemoBinding.inflate(layoutInflater, parent, false)
//            val viewHolderMemoSearch = ViewHolderMemoSearch(rowMemoBinding)
//            rowMemoBinding.root.setOnClickListener(viewHolderMemoSearch)
//            return viewHolderMemoSearch
//        }


    // Recyclerview의 어뎁터
    inner class RecyclerViewMemoSearchAdapter : RecyclerView.Adapter<RecyclerViewMemoSearchAdapter.ViewHolderMemoSearchAdapter>(){

        // ViewHolder
        inner class ViewHolderMemoSearchAdapter(val rowMemoBinding: RowMemoBinding) : RecyclerView.ViewHolder(rowMemoBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMemoSearchAdapter {
            val rowMemoBinding = RowMemoBinding.inflate(layoutInflater, parent, false)
            val viewHolderMemoSearchAdapter = ViewHolderMemoSearchAdapter(rowMemoBinding)

            rowMemoBinding.root.setOnClickListener {
//                // mainActivity.replaceFragment(FragmentName.READ_MEMO_FRAGMENT, true, true, null)
//                // 항목을 눌러 메모 보는 화면으로 이동하는 처리
                showMemoData(viewHolderMemoSearchAdapter.adapterPosition)
//
//           // 메모 번호를 전달한다.
//            val dataBundle = Bundle()
//            dataBundle.putInt("memoIdx", memoList[adapterPosition].memoIdx)
//            mainActivity.replaceFragment(FragmentName.READ_MEMO_FRAGMENT,true,true,dataBundle)
//
            }

            // 즐겨찾기 버튼 처리
            rowMemoBinding.buttonRowFavorite.setOnClickListener {
                // 사용자가 선택한 항목 번째 객체를 가져온다.
                val memoModel = memoList[viewHolderMemoSearchAdapter.adapterPosition]
                // 즐겨찾기 값을 반대값으로 넣어준다.
                memoModel.memoIsFavorite = !memoModel.memoIsFavorite
                // 즐겨찾기 값을 수정한다.
                CoroutineScope(Dispatchers.Main).launch {
                    val work1 = async(Dispatchers.IO){
                        MemoRepository.updateMemoFavorite(mainActivity, memoModel.memoIdx, memoModel.memoIsFavorite)
                    }
                    work1.join()

                    // 즐겨찾기 라면...
                    if(arguments?.getString("MemoName") == MemoListName.MEMO_NAME_FAVORITE.str){
                        // 현재 번째 객체를 제거한다.
                        memoList.removeAt(viewHolderMemoSearchAdapter.adapterPosition)
                        fragmentSearchMemoBinding.recyclerViewSearchMemo.adapter?.notifyItemRemoved(viewHolderMemoSearchAdapter.adapterPosition)
                    } else {
                        val a1 = rowMemoBinding.buttonRowFavorite as MaterialButton
                        if (memoModel.memoIsFavorite) {
                            a1.setIconResource(R.drawable.star_full_24px)
                        } else {
                            a1.setIconResource(R.drawable.star_24px)
                        }
                    }
                }
            }

            return viewHolderMemoSearchAdapter
        }


        override fun getItemCount(): Int {
            return memoList.size
        }

        override fun onBindViewHolder(holder: ViewHolderMemoSearchAdapter, position: Int) {
            holder.rowMemoBinding.textViewRowTitle.text = memoList[position].memoTitle

            val a1 = holder.rowMemoBinding.buttonRowFavorite as MaterialButton
            if(memoList[position].memoIsFavorite){
                a1.setIconResource(R.drawable.star_full_24px)
            } else {
                a1.setIconResource(R.drawable.star_24px)
            }
        }
    }

    // 입력 요소 설정
    fun settingTextField(){
        fragmentSearchMemoBinding.apply {
            // 검색창에 포커스를 준다.
            mainActivity.showSoftInput(textFieldSearchMemoTitle.editText!!)
            // 키보드의 엔터를 누르면 동작하는 리스너
            textFieldSearchMemoTitle.editText?.setOnEditorActionListener { v, actionId, event ->
                // 검색 데이터를 가져와 보여준다.
                CoroutineScope(Dispatchers.Main).launch {
                    val work1 = async(Dispatchers.IO){
                        val keyword = textFieldSearchMemoTitle.editText?.text.toString()
                        MemoRepository.selectMemoDataAllByMemoTitle(mainActivity, keyword)
                    }
                    memoList = work1.await()
                    recyclerViewSearchMemo.adapter?.notifyDataSetChanged()
                }
                mainActivity.hideSoftInput()
                true
            }
        }
    }

    // 항목을 눌러 메모 보는 화면으로 이동하는 처리
    fun showMemoData(position:Int){
        // 비밀 메모인지 확인한다.
        if(memoList[position].memoIsSecret){
            val builder = MaterialAlertDialogBuilder(mainActivity)
            builder.setTitle("비밀번호 입력")

            val dialogMemoPasswordBinding = DialogMemoPasswordBinding.inflate(layoutInflater)
            builder.setView(dialogMemoPasswordBinding.root)

            builder.setNegativeButton("취소", null)
            builder.setPositiveButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                // 사용자가 입력한 비밀번호를 가져온다.
                val inputPassword = dialogMemoPasswordBinding.textFieldDialogMemoPassword.editText?.text.toString()
                // 입력한 비밀번호를 제대로 입력했다면
                if(inputPassword == memoList[position].memoPassword){
                    // 메모 번호를 전달한다.
                    val dataBundle = Bundle()
                    dataBundle.putInt("memoIdx", memoList[position].memoIdx)
                    mainActivity.replaceFragment(FragmentName.READ_MEMO_FRAGMENT, true, true, dataBundle)
                } else {
                    val snackbar = Snackbar.make(mainActivity.activityMainBinding.root, "비밀번호를 잘못 입력하였습니다", Snackbar.LENGTH_SHORT)
                    snackbar.show()
                }
            }
            builder.show()
        } else {
            // 메모 번호를 전달한다.
            val dataBundle = Bundle()
            dataBundle.putInt("memoIdx", memoList[position].memoIdx)
            mainActivity.replaceFragment(FragmentName.READ_MEMO_FRAGMENT, true, true, dataBundle)
        }
    }

}